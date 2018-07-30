package com.jhzhang.address.normalizer.prob.bean;

/**
 * @author jhzhang
 */

@SuppressWarnings("ALL")
public enum AddressType {
    Municipality,  //直辖市
    SuffixMunicipality,  //特别行政区后缀
    Province,  //省
    City, //市
    County,//区
    Town,//镇
    District,//社区
    Street,//街
    No,//编号
    Symbol,//字母符号
    LandMark,//地标建筑 例如 ** 大厦  门牌设施
    RelatedPos,//相对位置
    Crossing,//交叉路
    DetailDesc,//详细描述
    childFacility,//子设施
    Village,//村
    Start,//开始状态
    End,//结束状态
    StartSuffix,//左括号(
    EndSuffix,//右括号)
    Unknow,
    Other,
    SuffixProvince,// 省后缀
    SuffixCity,//市后缀
    SuffixCounty,//区后缀
    SuffixDistrict,//区域后缀
    SuffixTown,//镇后缀
    SuffixStreet,//街后缀
    SuffixLandMark,//地标建筑后缀
    SuffixVillage,//村后缀
    SuffixBuilding,//J级别后缀，栋后缀
    SuffixBuildingUnit,//K级别，单元后缀
    SuffixFloor,// L级别，层后缀
    SuffixRoom,// M级别，房间后缀
    SuffixIndicationFacility,//指示性设施后缀
    IndicationFacility,//指示性设施
    SuffixIndicationPosition,//指示性设施方位后缀
    IndicationPosition,//指示性设施方位
    Conj,//连接词
}
