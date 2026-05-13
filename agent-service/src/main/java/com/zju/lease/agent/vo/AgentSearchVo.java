package com.zju.lease.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "AI搜索返回结果")
@Data
@AllArgsConstructor
public class AgentSearchVo {

    @Schema(description = "AI推荐的文本内容")
    private String aiText;

    @Schema(description = "匹配的房间列表")
    private List<RoomItemVo> rooms;

    @Data
    @AllArgsConstructor
    public static class RoomItemVo {
        @Schema(description = "房间ID")
        private Long id;

        @Schema(description = "房间号")
        private String roomNumber;

        @Schema(description = "月租金")
        private BigDecimal rent;

        @Schema(description = "公寓名称")
        private String apartmentName;

        @Schema(description = "省份名称")
        private String provinceName;

        @Schema(description = "城市名称")
        private String cityName;

        @Schema(description = "区域名称")
        private String districtName;

        @Schema(description = "标签列表")
        private List<String> labels;

        @Schema(description = "首张图片URL")
        private String imageUrl;
    }
}
