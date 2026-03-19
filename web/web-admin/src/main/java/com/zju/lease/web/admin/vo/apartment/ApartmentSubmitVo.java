package com.zju.lease.web.admin.vo.apartment;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.zju.lease.model.entity.*;
import com.zju.lease.web.admin.vo.fee.FeeValueVo;
import com.zju.lease.web.admin.vo.graph.GraphVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "公寓信息")
@Data
public class ApartmentSubmitVo extends ApartmentInfo {

    @Schema(description="公寓配套id")
    private List<FacilityInfo> facilityInfoList;

    @Schema(description="公寓标签id")
    @JsonProperty("labelInfoList")
    private List<LabelInfo> labelInfoList;

    @Schema(description="公寓杂费值id")
    private List<FeeValueVo> feeValueVoList;

    @Schema(description="公寓图片id")
    private List<GraphVo> graphVoList;

}
