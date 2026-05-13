package com.zju.lease.agent.service;

import com.zju.lease.agent.mapper.*;
import com.zju.lease.model.entity.*;
import com.zju.lease.model.enums.ReleaseStatus;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApartmentDataIngestor {

    private final RoomInfoMapper roomInfoMapper;
    private final ApartmentInfoMapper apartmentInfoMapper;
    private final RoomAttrValueMapper roomAttrValueMapper;
    private final RoomFacilityMapper roomFacilityMapper;
    private final RoomLabelMapper roomLabelMapper;
    private final RoomPaymentTypeMapper roomPaymentTypeMapper;
    private final RoomLeaseTermMapper roomLeaseTermMapper;
    private final ApartmentFacilityMapper apartmentFacilityMapper;
    private final ApartmentLabelMapper apartmentLabelMapper;
    private final ApartmentFeeValueMapper apartmentFeeValueMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public void ingestRoom(Long roomId) {
        try {
            RoomInfo room = roomInfoMapper.selectById(roomId);
            if (room == null || room.getIsRelease() != ReleaseStatus.RELEASED) {
                removeRoomFromIndex(roomId);
                return;
            }
            removeRoomFromIndex(roomId);

            String document = buildFullRoomDocument(room);
            if (document == null) return;

            TextSegment segment = TextSegment.from(document, createMetadata(room));
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
            log.info("Ingested room {} into vector store", roomId);
        } catch (Exception e) {
            log.error("Failed to ingest room {}", roomId, e);
        }
    }

    private Metadata createMetadata(RoomInfo room) {
        Metadata metadata = new Metadata();
        metadata.put("roomId", room.getId().toString());
        metadata.put("apartmentId", room.getApartmentId().toString());
        metadata.put("rent", room.getRent().toString());
        return metadata;
    }

    private void removeRoomFromIndex(Long roomId) {
        try {
            embeddingStore.removeAll(new IsEqualTo("roomId", roomId.toString()));
        } catch (Exception e) {
            log.warn("Failed to remove old embeddings for room {}: {}", roomId, e.getMessage());
        }
    }

    private String buildFullRoomDocument(RoomInfo room) {
        ApartmentInfo apartment = apartmentInfoMapper.selectById(room.getApartmentId());
        if (apartment == null) {
            log.warn("Apartment {} not found for room {}", room.getApartmentId(), room.getId());
            return null;
        }

        List<AttrValue> attrValues = roomAttrValueMapper.selectListByRoomId(room.getId());
        List<FacilityInfo> roomFacilities = roomFacilityMapper.selectListByRoomId(room.getId());
        List<LabelInfo> roomLabels = roomLabelMapper.selectListByRoomId(room.getId());
        List<PaymentType> paymentTypes = roomPaymentTypeMapper.selectListByRoomId(room.getId());
        List<LeaseTerm> leaseTerms = roomLeaseTermMapper.selectListByRoomId(room.getId());
        List<FacilityInfo> apartmentFacilities = apartmentFacilityMapper.selectListByApartmentId(apartment.getId());
        List<LabelInfo> apartmentLabels = apartmentLabelMapper.selectListByApartmentId(apartment.getId());
        List<FeeValue> feeValues = apartmentFeeValueMapper.selectListByApartmentId(apartment.getId());

        return buildRoomDocument(room, apartment, attrValues, roomFacilities, roomLabels,
                paymentTypes, leaseTerms, apartmentFacilities, apartmentLabels, feeValues);
    }

    private String buildRoomDocument(RoomInfo room, ApartmentInfo apartment,
                                      List<AttrValue> attrValues,
                                      List<FacilityInfo> roomFacilities,
                                      List<LabelInfo> roomLabels,
                                      List<PaymentType> paymentTypes,
                                      List<LeaseTerm> leaseTerms,
                                      List<FacilityInfo> apartmentFacilities,
                                      List<LabelInfo> apartmentLabels,
                                      List<FeeValue> feeValues) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("房间 %s，", room.getRoomNumber()));
        sb.append(String.format("位于%s%s%s，", apartment.getProvinceName(), apartment.getCityName(), apartment.getDistrictName()));
        sb.append(String.format("详细地址%s，", apartment.getAddressDetail()));
        sb.append(String.format("月租金%.0f元。", room.getRent()));

        if (!attrValues.isEmpty()) {
            String attrs = attrValues.stream()
                    .map(ApartmentDataIngestor::formatAttrValue)
                    .collect(Collectors.joining("，"));
            sb.append("房间属性：").append(attrs).append("。");
        }

        if (!roomFacilities.isEmpty()) {
            String facilities = roomFacilities.stream()
                    .map(FacilityInfo::getName)
                    .collect(Collectors.joining("、"));
            sb.append("房间配套：").append(facilities).append("。");
        }

        if (!roomLabels.isEmpty()) {
            String labels = roomLabels.stream()
                    .map(LabelInfo::getName)
                    .collect(Collectors.joining("、"));
            sb.append("房间特点：").append(labels).append("。");
        }

        if (!apartmentFacilities.isEmpty()) {
            String aptFacilities = apartmentFacilities.stream()
                    .map(FacilityInfo::getName)
                    .collect(Collectors.joining("、"));
            sb.append("公寓配套：").append(aptFacilities).append("。");
        }

        if (!apartmentLabels.isEmpty()) {
            String aptLabels = apartmentLabels.stream()
                    .map(LabelInfo::getName)
                    .collect(Collectors.joining("、"));
            sb.append("公寓特点：").append(aptLabels).append("。");
        }

        if (!paymentTypes.isEmpty()) {
            String payments = paymentTypes.stream()
                    .map(PaymentType::getName)
                    .collect(Collectors.joining("、"));
            sb.append("支持支付方式：").append(payments).append("。");
        }

        if (!leaseTerms.isEmpty()) {
            String terms = leaseTerms.stream()
                    .map(lt -> lt.getMonthCount() + "个月")
                    .collect(Collectors.joining("、"));
            sb.append("可选租期：").append(terms).append("。");
        }

        if (!feeValues.isEmpty()) {
            String fees = feeValues.stream()
                    .map(fv -> {
                        String s = fv.getName();
                        if (fv.getUnit() != null && !fv.getUnit().isEmpty()) {
                            s += "(" + fv.getUnit() + ")";
                        }
                        return s;
                    })
                    .collect(Collectors.joining("、"));
            sb.append("杂费说明：").append(fees).append("。");
        }

        if (apartment.getIntroduction() != null && !apartment.getIntroduction().isEmpty()) {
            sb.append("公寓介绍：").append(apartment.getIntroduction()).append("。");
        }

        sb.append("房东可联系。");
        return sb.toString();
    }

    private static String formatAttrValue(AttrValue av) {
        return av.getName();
    }

    @Async
    public void ingestRoomAsync(Long roomId) {
        ingestRoom(roomId);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void fullReindex() {
        log.info("Starting full apartment/room re-index...");
        List<RoomInfo> rooms = roomInfoMapper.selectAllReleasedRooms();
        for (RoomInfo room : rooms) {
            ingestRoom(room.getId());
        }
        log.info("Full re-index completed. {} rooms ingested.", rooms.size());
    }
}
