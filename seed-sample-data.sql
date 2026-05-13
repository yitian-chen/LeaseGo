-- ============================================================
-- 示例房源数据脚本（杭州）
-- 包含5个公寓 + 18个房间及其完整的关联数据
-- ============================================================
-- 使用方法: mysql -u root -p lease < seed-sample-data.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @now = NOW();

-- ==================== 公寓数据 ====================
-- 现有最大id为15，从16开始

INSERT INTO `apartment_info` (`id`, `name`, `introduction`, `province_id`, `city_id`, `district_id`, `district_name`, `address_detail`, `latitude`, `longitude`, `phone`, `is_release`, `create_time`) VALUES
(16, '西溪银泰公寓', '西溪银泰城旁高端公寓，周边配套齐全，交通便利，近地铁5号线', 33, 3301, 330106, '西湖区', '西湖区余杭塘路与崇仁路交叉口西溪银泰城', '30.2741', '120.0717', '0571-88880001', 1, @now),
(17, '未来科技城人才公寓', '坐落于杭州未来科技城核心区，周边阿里、字节等大厂环绕，通勤便利', 33, 3301, 330110, '余杭区', '余杭区文一西路与良睦路交叉口', '30.2818', '120.0096', '0571-88880002', 1, @now),
(18, '运河人家公寓', '拱墅区运河畔高品质公寓，环境优雅，生活便利', 33, 3301, 330105, '拱墅区', '拱墅区湖墅南路与卖鱼桥交叉口', '30.3003', '120.1465', '0571-88880003', 1, @now),
(19, '钱塘江畔青年社区', '滨江区钱塘江畔精品公寓，年轻时尚，适合互联网从业者', 33, 3301, 330108, '滨江区', '滨江区江南大道与江晖路交叉口', '30.2086', '120.2120', '0571-88880004', 1, @now),
(20, '奥体之星公寓', '萧山区奥体板块高端服务式公寓，亚运核心区，地铁直达', 33, 3301, 330109, '萧山区', '萧山区市心北路与奔竞大道交叉口', '30.2403', '120.2536', '0571-88880005', 1, @now);


-- ==================== 房间数据 ====================
-- 现有最大id为22，从23开始

-- === 西溪银泰公寓 (id=16) ===
INSERT INTO `room_info` (`id`, `room_number`, `rent`, `apartment_id`, `is_release`, `landlord_id`, `create_time`) VALUES
(23, 'A-101', 1800, 16, 1, 8, @now),
(24, 'A-102', 2200, 16, 1, 8, @now),
(25, 'A-201', 2500, 16, 1, 9, @now),
(26, 'B-101', 1500, 16, 1, 9, @now);

-- === 未来科技城人才公寓 (id=17) ===
INSERT INTO `room_info` (`id`, `room_number`, `rent`, `apartment_id`, `is_release`, `landlord_id`, `create_time`) VALUES
(27, '1-101', 2800, 17, 1, 10, @now),
(28, '1-102', 3200, 17, 1, 10, @now),
(29, '2-201', 3500, 17, 1, 8, @now),
(30, '2-202', 2600, 17, 1, 8, @now);

-- === 运河人家公寓 (id=18) ===
INSERT INTO `room_info` (`id`, `room_number`, `rent`, `apartment_id`, `is_release`, `landlord_id`, `create_time`) VALUES
(31, '1-101', 2000, 18, 1, 9, @now),
(32, '1-102', 2400, 18, 1, 9, @now),
(33, '2-201', 3000, 18, 1, 10, @now),
(34, '3-301', 3800, 18, 1, 10, @now);

-- === 钱塘江畔青年社区 (id=19) ===
INSERT INTO `room_info` (`id`, `room_number`, `rent`, `apartment_id`, `is_release`, `landlord_id`, `create_time`) VALUES
(35, 'A-101', 2200, 19, 1, 8, @now),
(36, 'A-102', 2500, 19, 1, 8, @now),
(37, 'B-201', 3000, 19, 1, 10, @now);

-- === 奥体之星公寓 (id=20) ===
INSERT INTO `room_info` (`id`, `room_number`, `rent`, `apartment_id`, `is_release`, `landlord_id`, `create_time`) VALUES
(38, '1-101', 3500, 20, 1, 9, @now),
(39, '1-102', 4000, 20, 1, 9, @now),
(40, '2-201', 5000, 20, 1, 8, @now);


-- ==================== 房间属性关联 (room_attr_value) ====================
-- attr_key: 5-户型, 7-面积, 8-朝向, 9-采光, 10-卫所

-- 房间23（A-101）: 一室一厅/25平/南/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(23, 8, @now), (23, 15, @now), (23, 20, @now), (23, 24, @now), (23, 27, @now);

-- 房间24（A-102）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(24, 9, @now), (24, 16, @now), (24, 20, @now), (24, 23, @now), (24, 27, @now);

-- 房间25（A-201）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(25, 9, @now), (25, 16, @now), (25, 20, @now), (25, 23, @now), (25, 27, @now);

-- 房间26（B-101）: 一室一厅/25平/东/良/公共
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(26, 8, @now), (26, 15, @now), (26, 19, @now), (26, 24, @now), (26, 28, @now);

-- 房间27（1-101）: 一室一厅/25平/南/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(27, 8, @now), (27, 15, @now), (27, 20, @now), (27, 24, @now), (27, 27, @now);

-- 房间28（1-102）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(28, 9, @now), (28, 16, @now), (28, 20, @now), (28, 23, @now), (28, 27, @now);

-- 房间29（2-201）: 三室一厅/60平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(29, 10, @now), (29, 17, @now), (29, 20, @now), (29, 23, @now), (29, 27, @now);

-- 房间30（2-202）: 一室一厅/25平/北/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(30, 8, @now), (30, 15, @now), (30, 22, @now), (30, 24, @now), (30, 27, @now);

-- 房间31（1-101）: 一室一厅/25平/南/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(31, 8, @now), (31, 15, @now), (31, 20, @now), (31, 24, @now), (31, 27, @now);

-- 房间32（1-102）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(32, 9, @now), (32, 16, @now), (32, 20, @now), (32, 23, @now), (32, 27, @now);

-- 房间33（2-201）: 两室一厅/35平/东/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(33, 9, @now), (33, 16, @now), (33, 19, @now), (33, 24, @now), (33, 27, @now);

-- 房间34（3-301）: 三室一厅/80平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(34, 10, @now), (34, 18, @now), (34, 20, @now), (34, 23, @now), (34, 27, @now);

-- 房间35（A-101）: 一室一厅/25平/南/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(35, 8, @now), (35, 15, @now), (35, 20, @now), (35, 24, @now), (35, 27, @now);

-- 房间36（A-102）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(36, 9, @now), (36, 16, @now), (36, 20, @now), (36, 23, @now), (36, 27, @now);

-- 房间37（B-201）: 两室一厅/60平/西/良/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(37, 9, @now), (37, 17, @now), (37, 21, @now), (37, 24, @now), (37, 27, @now);

-- 房间38（1-101）: 两室一厅/35平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(38, 9, @now), (38, 16, @now), (38, 20, @now), (38, 23, @now), (38, 27, @now);

-- 房间39（1-102）: 两室一厅/35平/南/优/阳台
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(39, 9, @now), (39, 16, @now), (39, 20, @now), (39, 23, @now), (39, 27, @now);

-- 房间40（2-201）: 三室一厅/80平/南/优/独卫
INSERT INTO `room_attr_value` (`room_id`, `attr_value_id`, `create_time`) VALUES
(40, 10, @now), (40, 18, @now), (40, 20, @now), (40, 23, @now), (40, 27, @now);


-- ==================== 房间设施关联 (room_facility) ====================
-- facility type=2: 28-空调,29-洗衣机,30-冰箱,48-书桌,49-WIFI,50-床,51-沙发,52-微波炉,53-油烟机,54-热水器,55-衣柜,56-电视机

-- 房间23: 空调 洗衣机 WIFI 床 热水器 衣柜 冰箱
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(23, 28, @now), (23, 29, @now), (23, 49, @now), (23, 50, @now), (23, 54, @now), (23, 55, @now), (23, 30, @now);
-- 房间24: 空调 洗衣机 WIFI 床 热水器 衣柜 书桌 冰箱
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(24, 28, @now), (24, 29, @now), (24, 49, @now), (24, 50, @now), (24, 54, @now), (24, 55, @now), (24, 48, @now), (24, 30, @now);
-- 房间25: 空调 洗衣机 WIFI 床 热水器 衣柜 沙发 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(25, 28, @now), (25, 29, @now), (25, 49, @now), (25, 50, @now), (25, 54, @now), (25, 55, @now), (25, 51, @now), (25, 48, @now);
-- 房间26: 空调 床 WIFI 热水器
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(26, 28, @now), (26, 50, @now), (26, 49, @now), (26, 54, @now);
-- 房间27: 空调 洗衣机 WIFI 床 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(27, 28, @now), (27, 29, @now), (27, 49, @now), (27, 50, @now), (27, 54, @now), (27, 55, @now), (27, 48, @now);
-- 房间28: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(28, 28, @now), (28, 29, @now), (28, 30, @now), (28, 49, @now), (28, 50, @now), (28, 51, @now), (28, 54, @now), (28, 55, @now), (28, 48, @now);
-- 房间29: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 电视机 书桌 微波炉
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(29, 28, @now), (29, 29, @now), (29, 30, @now), (29, 49, @now), (29, 50, @now), (29, 51, @now), (29, 54, @now), (29, 55, @now), (29, 56, @now), (29, 48, @now), (29, 52, @now);
-- 房间30: 空调 洗衣机 WIFI 床 热水器
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(30, 28, @now), (30, 29, @now), (30, 49, @now), (30, 50, @now), (30, 54, @now);
-- 房间31: 空调 洗衣机 WIFI 床 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(31, 28, @now), (31, 29, @now), (31, 49, @now), (31, 50, @now), (31, 54, @now), (31, 55, @now), (31, 48, @now);
-- 房间32: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 电视机
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(32, 28, @now), (32, 29, @now), (32, 30, @now), (32, 49, @now), (32, 50, @now), (32, 51, @now), (32, 54, @now), (32, 55, @now), (32, 56, @now);
-- 房间33: 空调 洗衣机 WIFI 床 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(33, 28, @now), (33, 29, @now), (33, 49, @now), (33, 50, @now), (33, 54, @now), (33, 55, @now), (33, 48, @now);
-- 房间34: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 电视机 书桌 微波炉 油烟机
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(34, 28, @now), (34, 29, @now), (34, 30, @now), (34, 49, @now), (34, 50, @now), (34, 51, @now), (34, 54, @now), (34, 55, @now), (34, 56, @now), (34, 48, @now), (34, 52, @now), (34, 53, @now);
-- 房间35: 空调 洗衣机 WIFI 床 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(35, 28, @now), (35, 29, @now), (35, 49, @now), (35, 50, @now), (35, 54, @now), (35, 55, @now), (35, 48, @now);
-- 房间36: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 书桌
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(36, 28, @now), (36, 29, @now), (36, 30, @now), (36, 49, @now), (36, 50, @now), (36, 51, @now), (36, 54, @now), (36, 55, @now), (36, 48, @now);
-- 房间37: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 书桌 微波炉
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(37, 28, @now), (37, 29, @now), (37, 30, @now), (37, 49, @now), (37, 50, @now), (37, 51, @now), (37, 54, @now), (37, 48, @now), (37, 52, @now);
-- 房间38: 空调 洗衣机 冰箱 WIFI 床 热水器 衣柜
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(38, 28, @now), (38, 29, @now), (38, 30, @now), (38, 49, @now), (38, 50, @now), (38, 54, @now), (38, 55, @now);
-- 房间39: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 电视机
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(39, 28, @now), (39, 29, @now), (39, 30, @now), (39, 49, @now), (39, 50, @now), (39, 51, @now), (39, 54, @now), (39, 55, @now), (39, 56, @now);
-- 房间40: 空调 洗衣机 冰箱 WIFI 床 沙发 热水器 衣柜 电视机 书桌 微波炉 油烟机
INSERT INTO `room_facility` (`room_id`, `facility_id`, `create_time`) VALUES
(40, 28, @now), (40, 29, @now), (40, 30, @now), (40, 49, @now), (40, 50, @now), (40, 51, @now), (40, 54, @now), (40, 55, @now), (40, 56, @now), (40, 48, @now), (40, 52, @now), (40, 53, @now);


-- ==================== 房间标签关联 (room_label) ====================
-- label type=2: 5-朝南,6-朝北,7-朝东,10-朝西,15-独卫,16-阳台

-- 朝南的房间: 23,24,25,27,28,29,31,32,34,35,36,38,39,40
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES
(23, 5, @now), (24, 5, @now), (25, 5, @now), (27, 5, @now), (28, 5, @now),
(29, 5, @now), (31, 5, @now), (32, 5, @now), (34, 5, @now), (35, 5, @now),
(36, 5, @now), (38, 5, @now), (39, 5, @now), (40, 5, @now);
-- 朝北: 30
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES (30, 6, @now);
-- 朝东: 26,33
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES (26, 7, @now), (33, 7, @now);
-- 朝西: 37
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES (37, 10, @now);
-- 独卫: 23,24,25,27,28,29,30,31,32,33,34,35,36,37,38,40 (除26外全部)
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES
(23, 15, @now), (24, 15, @now), (25, 15, @now), (27, 15, @now), (28, 15, @now),
(29, 15, @now), (30, 15, @now), (31, 15, @now), (32, 15, @now), (33, 15, @now),
(34, 15, @now), (35, 15, @now), (36, 15, @now), (37, 15, @now), (38, 15, @now),
(40, 15, @now);
-- 阳台: 25,29,34,39,40
INSERT INTO `room_label` (`room_id`, `label_id`, `create_time`) VALUES
(25, 16, @now), (29, 16, @now), (34, 16, @now), (39, 16, @now), (40, 16, @now);


-- ==================== 房间支付方式关联 (room_payment_type) ====================
-- 6-月付,7-季付,8-半年付,10-年付

-- 低租金房间(<=2000): 支持月付+季付
INSERT INTO `room_payment_type` (`room_id`, `payment_type_id`, `create_time`) VALUES
(23, 6, @now), (23, 7, @now),
(26, 6, @now), (26, 7, @now),
(31, 6, @now), (31, 7, @now),
(35, 6, @now), (35, 7, @now);
-- 中租金房间(2000-3500): 月付+季付+半年付
INSERT INTO `room_payment_type` (`room_id`, `payment_type_id`, `create_time`) VALUES
(24, 6, @now), (24, 7, @now), (24, 8, @now),
(25, 6, @now), (25, 7, @now), (25, 8, @now),
(27, 6, @now), (27, 7, @now), (27, 8, @now),
(28, 6, @now), (28, 7, @now), (28, 8, @now),
(30, 6, @now), (30, 7, @now), (30, 8, @now),
(32, 6, @now), (32, 7, @now), (32, 8, @now),
(33, 6, @now), (33, 7, @now), (33, 8, @now),
(36, 6, @now), (36, 7, @now), (36, 8, @now),
(37, 6, @now), (37, 7, @now), (37, 8, @now),
(38, 6, @now), (38, 7, @now), (38, 8, @now);
-- 高租金房间(>=3500): 全部支持
INSERT INTO `room_payment_type` (`room_id`, `payment_type_id`, `create_time`) VALUES
(29, 6, @now), (29, 7, @now), (29, 8, @now), (29, 10, @now),
(34, 6, @now), (34, 7, @now), (34, 8, @now), (34, 10, @now),
(39, 6, @now), (39, 7, @now), (39, 8, @now), (39, 10, @now),
(40, 6, @now), (40, 7, @now), (40, 8, @now), (40, 10, @now);


-- ==================== 房间租期关联 (room_lease_term) ====================
-- 1-1月,3-3月,4-6月,8-12月

-- 大部分房间支持3月+6月+12月
INSERT INTO `room_lease_term` (`room_id`, `lease_term_id`, `create_time`) VALUES
(23, 3, @now), (23, 4, @now), (23, 8, @now),
(24, 3, @now), (24, 4, @now), (24, 8, @now),
(25, 3, @now), (25, 4, @now), (25, 8, @now),
(26, 3, @now), (26, 4, @now),
(27, 3, @now), (27, 4, @now), (27, 8, @now),
(28, 3, @now), (28, 4, @now), (28, 8, @now),
(29, 3, @now), (29, 4, @now), (29, 8, @now),
(30, 3, @now), (30, 4, @now), (30, 8, @now),
(31, 3, @now), (31, 4, @now), (31, 8, @now),
(32, 3, @now), (32, 4, @now), (32, 8, @now),
(33, 3, @now), (33, 4, @now), (33, 8, @now),
(34, 3, @now), (34, 4, @now), (34, 8, @now),
(35, 3, @now), (35, 4, @now), (35, 8, @now),
(36, 3, @now), (36, 4, @now), (36, 8, @now),
(37, 3, @now), (37, 4, @now), (37, 8, @now),
(38, 3, @now), (38, 4, @now), (38, 8, @now),
(39, 3, @now), (39, 4, @now), (39, 8, @now),
(40, 3, @now), (40, 4, @now), (40, 8, @now);
-- 部分房间支持1月短租
INSERT INTO `room_lease_term` (`room_id`, `lease_term_id`, `create_time`) VALUES
(23, 1, @now), (26, 1, @now), (31, 1, @now), (35, 1, @now);


-- ==================== 公寓设施关联 (apartment_facility) ====================
-- facility type=1: 24-健身房,25-停车位,26-电梯,40-台球,41-安保,42-团建,43-书吧,44-休息室,45-便利店,46-休闲区,47-监控,57-智能锁

-- 公寓16(西溪银泰): 健身房 电梯 安保 监控 便利店 书吧 休息室 智能锁 停车位
INSERT INTO `apartment_facility` (`apartment_id`, `facility_id`, `create_time`) VALUES
(16, 24, @now), (16, 26, @now), (16, 41, @now), (16, 47, @now), (16, 45, @now),
(16, 43, @now), (16, 44, @now), (16, 57, @now), (16, 25, @now);
-- 公寓17(未来科技城): 健身房 电梯 安保 监控 休闲区 智能锁 停车位
INSERT INTO `apartment_facility` (`apartment_id`, `facility_id`, `create_time`) VALUES
(17, 24, @now), (17, 26, @now), (17, 41, @now), (17, 47, @now),
(17, 46, @now), (17, 57, @now), (17, 25, @now);
-- 公寓18(运河人家): 电梯 安保 监控 书吧 团建 休息室
INSERT INTO `apartment_facility` (`apartment_id`, `facility_id`, `create_time`) VALUES
(18, 26, @now), (18, 41, @now), (18, 47, @now),
(18, 43, @now), (18, 42, @now), (18, 44, @now);
-- 公寓19(钱塘江畔): 健身房 电梯 安保 监控 台球 休闲区 书吧 便利店 智能锁
INSERT INTO `apartment_facility` (`apartment_id`, `facility_id`, `create_time`) VALUES
(19, 24, @now), (19, 26, @now), (19, 41, @now), (19, 47, @now), (19, 40, @now),
(19, 46, @now), (19, 43, @now), (19, 45, @now), (19, 57, @now);
-- 公寓20(奥体之星): 健身房 电梯 安保 监控 台球 书吧 休息室 智能锁 停车位 休闲区
INSERT INTO `apartment_facility` (`apartment_id`, `facility_id`, `create_time`) VALUES
(20, 24, @now), (20, 26, @now), (20, 41, @now), (20, 47, @now), (20, 40, @now),
(20, 43, @now), (20, 44, @now), (20, 57, @now), (20, 25, @now), (20, 46, @now);


-- ==================== 公寓标签关联 (apartment_label) ====================
-- label type=1: 1-近地铁,2-近公交,3-有电梯,4-停车场

-- 公寓16(西溪银泰): 近地铁 近公交 有电梯
INSERT INTO `apartment_label` (`apartment_id`, `label_id`, `create_time`) VALUES
(16, 1, @now), (16, 2, @now), (16, 3, @now);
-- 公寓17(未来科技城): 近地铁 近公交 有电梯 停车场
INSERT INTO `apartment_label` (`apartment_id`, `label_id`, `create_time`) VALUES
(17, 1, @now), (17, 2, @now), (17, 3, @now), (17, 4, @now);
-- 公寓18(运河人家): 近地铁 近公交 有电梯
INSERT INTO `apartment_label` (`apartment_id`, `label_id`, `create_time`) VALUES
(18, 1, @now), (18, 2, @now), (18, 3, @now);
-- 公寓19(钱塘江畔): 近地铁 有电梯 停车场
INSERT INTO `apartment_label` (`apartment_id`, `label_id`, `create_time`) VALUES
(19, 1, @now), (19, 3, @now), (19, 4, @now);
-- 公寓20(奥体之星): 近地铁 有电梯 停车场
INSERT INTO `apartment_label` (`apartment_id`, `label_id`, `create_time`) VALUES
(20, 1, @now), (20, 3, @now), (20, 4, @now);


-- ==================== 公寓杂费关联 (apartment_fee_value) ====================
-- 停车费(id:1-400元/月,3-200元/月), 网费(id:4-50元/月), 电费(id:13-1.5元/度,15-0.5元/度), 水费(id:16-10元/吨,18-8元/吨)

-- 公寓16: 停车200元/月 网费50元/月 电1.5元/度 水10元/吨
INSERT INTO `apartment_fee_value` (`apartment_id`, `fee_value_id`, `create_time`) VALUES
(16, 3, @now), (16, 4, @now), (16, 13, @now), (16, 16, @now);
-- 公寓17: 停车200元/月 网费50元/月 电1元/度 水9元/吨
INSERT INTO `apartment_fee_value` (`apartment_id`, `fee_value_id`, `create_time`) VALUES
(17, 3, @now), (17, 4, @now), (17, 14, @now), (17, 17, @now);
-- 公寓18: 网费50元/月 电0.5元/度 水8元/吨
INSERT INTO `apartment_fee_value` (`apartment_id`, `fee_value_id`, `create_time`) VALUES
(18, 4, @now), (18, 15, @now), (18, 18, @now);
-- 公寓19: 停车300元/月 网费50元/月 电1.5元/度 水10元/吨
INSERT INTO `apartment_fee_value` (`apartment_id`, `fee_value_id`, `create_time`) VALUES
(19, 2, @now), (19, 4, @now), (19, 13, @now), (19, 16, @now);
-- 公寓20: 停车400元/月 网费60元/月 电1.5元/度 水10元/吨 取暖1500元/年
INSERT INTO `apartment_fee_value` (`apartment_id`, `fee_value_id`, `create_time`) VALUES
(20, 1, @now), (20, 5, @now), (20, 13, @now), (20, 16, @now), (20, 22, @now);


SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 验证数据 ====================
SELECT '新公寓数' AS stat, COUNT(*) AS cnt FROM apartment_info WHERE id >= 16
UNION ALL
SELECT '新房间数', COUNT(*) FROM room_info WHERE id >= 23
UNION ALL
SELECT '房间属性关联', COUNT(*) FROM room_attr_value WHERE room_id >= 23
UNION ALL
SELECT '房间设施关联', COUNT(*) FROM room_facility WHERE room_id >= 23
UNION ALL
SELECT '房间标签关联', COUNT(*) FROM room_label WHERE room_id >= 23
UNION ALL
SELECT '支付方式关联', COUNT(*) FROM room_payment_type WHERE room_id >= 23
UNION ALL
SELECT '租期关联', COUNT(*) FROM room_lease_term WHERE room_id >= 23
UNION ALL
SELECT '公寓设施关联', COUNT(*) FROM apartment_facility WHERE apartment_id >= 16
UNION ALL
SELECT '公寓标签关联', COUNT(*) FROM apartment_label WHERE apartment_id >= 16
UNION ALL
SELECT '杂费关联', COUNT(*) FROM apartment_fee_value WHERE apartment_id >= 16;
