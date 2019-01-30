package com.emotion.ecm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsmpp.bean.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitSmDto {

    private String messageId;
    private String serviceType;
    private TypeOfNumber destinationTon;
    private NumberingPlanIndicator destinationNpi;
    private String destinationNumber;
    private TypeOfNumber sourceTon;
    private NumberingPlanIndicator sourceNpi;
    private String sourceNumber;
    private ESMClass esmClass;
    private byte protocolId;
    private byte priorityFlag;
    private String scheduleDeliveryTime;
    private String validityPeriod;
    private RegisteredDelivery registeredDelivery;
    private byte replaceIfPresentFlag;
    private DataCoding dataCoding;
    private byte smDefaultMsgId;
    private byte[] shortMessage;
    private OptionalParameter[] optionalParameters;
}
