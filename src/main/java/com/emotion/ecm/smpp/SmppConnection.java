package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.*;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class SmppConnection extends SMPPSession implements MessageReceiverListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppConnection.class);

    private static final String PDU_EXCEPTION_CAPTION = "PduException message from Address: %s to Address: %s ;";
    private static final String RESPONSE_TIMEOUT_CAPTION = "ResponseTimeout from Address: %s to Address: %s ;";
    private static final String INVALID_RESPONSE_CAPTION = "InvalidResponse from Address: %s to Address: %s ;";
    private static final String NEGATIVE_RESPONSE_CAPTION = "NegativeResponse from Address: %s to Address: %s ;";
    private static final String IO_EXCEPTION_CAPTION = "IOException from Address: %s to Address: %s ;";

    private final SmscAccountDto smscAccount;
    private final BindParameter bindParameter;
    private final SmppService smppService;

    SmppConnection(SmscAccountDto smscAccountDto, SmppService smppService) {
        //TODO: 03-01-2018 Verify if "no sync" is working properly.
        super();
        this.smscAccount = smscAccountDto;
        this.bindParameter = new BindParameter(BindType.BIND_TRX, smscAccountDto.getSystemId(), smscAccountDto.getPassword(),
                smscAccountDto.getSystemType(), TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "");
        this.smppService = smppService;

        setMessageReceiverListener(this);
    }

    void sendBulkMessages(SubmitSmDto[] messagePack) throws SendingException {
        if (!bind()) {
            throw new SendingException(smscAccount + ": connection is not bound");
        }
        if (smscAccount.isAsynchronous()) {
            sendAsync(messagePack);
        } else {
            sendSync(messagePack);
        }
    }

    private void sendOneMessage(SubmitSmDto dto) throws SendingException {
        try {
            String messageId = submitShortMessage(
                    dto.getServiceType(),
                    dto.getSourceTon(),
                    dto.getSourceNpi(),
                    dto.getSourceNumber(),
                    dto.getDestinationTon(),
                    dto.getDestinationNpi(),
                    dto.getDestinationNumber(),
                    dto.getEsmClass(),
                    dto.getProtocolId(),
                    dto.getPriorityFlag(),
                    dto.getScheduleDeliveryTime(),
                    dto.getValidityPeriod(),
                    dto.getRegisteredDelivery(),
                    dto.getReplaceIfPresentFlag(),
                    dto.getDataCoding(),
                    dto.getSmDefaultMsgId(),
                    dto.getShortMessage(),
                    dto.getOptionalParameters()
            );
            dto.setMessageId(messageId);
            dto.setSubmitRespTime(LocalDateTime.now());
        } catch (PDUException e) {
            LOGGER.warn(String.format(PDU_EXCEPTION_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber()), e);
        } catch (ResponseTimeoutException e) {
            LOGGER.warn(String.format(RESPONSE_TIMEOUT_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber()), e);
        } catch (InvalidResponseException e) {
            LOGGER.warn(String.format(INVALID_RESPONSE_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber()), e);
        } catch (NegativeResponseException e) {
            LOGGER.warn(String.format(NEGATIVE_RESPONSE_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber()), e);
        } catch (IOException e) {
            String errorMessage = String.format(IO_EXCEPTION_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber());
            LOGGER.warn(errorMessage);
            throw new SendingException(errorMessage);
        }
    }

    public void shutdownConnection() {
        try {
            sendOutbind(smscAccount.getSystemId(), smscAccount.getPassword());
        } catch (IOException e) {
            LOGGER.info("IOException on outbind: ", e);
        }
    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if (smppService == null) {
            LOGGER.error("SmppService not initialized");
            return;
        }
        try {
            DeliveryReceipt deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
            String messageId = deliveryReceipt.getId();
            String messageStatus = deliveryReceipt.getFinalStatus().name();
            smppService.processDeliverySm(messageId, messageStatus);
        } catch (InvalidDeliveryReceiptException e) {
            LOGGER.warn("Incorrect delivery receipt: ", e);
        }

    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {

    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
        return null;
    }

    private void sendSync(SubmitSmDto[] submitSms) throws SendingException {
        for (SubmitSmDto submitSm : submitSms) {
            sendOneMessage(submitSm);
        }
    }

    private void sendAsync(SubmitSmDto[] messagePack) {
        Arrays.stream(messagePack).parallel()
                .forEach(dto -> {
                    try {
                        sendOneMessage(dto);
                    } catch (SendingException e) {
                        LOGGER.error(e.getMessage());
                    }
                });
    }

    private boolean bind() {
        if (!getSessionState().isBound()) {
            try {
                connectAndBind(smscAccount.getIpAddress(), smscAccount.getPort(), bindParameter);
            } catch (IOException e) {
                String smscInfo = "[" +
                        smscAccount.getSystemId() +
                        ", " +
                        smscAccount.getIpAddress() +
                        ":" +
                        smscAccount.getPort() +
                        "]";
                LOGGER.error(String.format("%s bind failed: %s", smscInfo, e.getMessage()));
            }
        }
        return getSessionState().isBound();
    }

}
