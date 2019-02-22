package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.model.dto.SubmitSmDto;
import lombok.Getter;
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

    @Getter
    private final SmscAccountDto smscAccount;
    private final BindParameter bindParameter;

    private boolean isBound;
    private ReceivedListener<DeliverSm> dlrReceivedListener;

    SmppConnection(SmscAccountDto smscAccountDto) {
        //TODO: 03-01-2018 Verify if "no sync" is working properly.
        super();
        this.smscAccount = smscAccountDto;
        bindParameter = new BindParameter(BindType.BIND_TRX, smscAccountDto.getSystemId(), smscAccountDto.getPassword(),
                "ecm", TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "");

        setMessageReceiverListener(this);
    }

    void sendBulkMessages(SubmitSmDto[] messagePack) throws SendingException {
        if (!bind()) {
            throw new SendingException("Connection is shutdown.");
        }
        if (smscAccount.isAsynchronous()) {
            sendAsync(messagePack);
        } else {
            sendSync(messagePack);
        }
    }

    private void sendOneMessage(SubmitSmDto dto) {
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
            LOGGER.warn(String.format(IO_EXCEPTION_CAPTION, dto.getSourceNumber(), dto.getDestinationNumber()), e);
        }
    }

    public void shutdownConnection() {
        try {
            sendOutbind(smscAccount.getSystemId(), smscAccount.getPassword());
            isBound = getSessionState().isBound();
        } catch (IOException e) {
            LOGGER.info("IOException on outbind: ", e);
        }
    }

    public void addDeliverListener(ReceivedListener<DeliverSm> dlrReceivedListener) {
        this.dlrReceivedListener = dlrReceivedListener;
    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if (dlrReceivedListener != null) {
            dlrReceivedListener.onReceived(deliverSm);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(String.format("No DeliveryListener for SmppConnection: %s;", smscAccount.toString()));
            }
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("Received DeliverySm: %s; ", deliverSm.getShortMessageAsDeliveryReceipt().toString()));
                }
            } catch (InvalidDeliveryReceiptException e) {
                LOGGER.warn("Incorrect delivery receipt: ", e);
            }
        }
    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {

    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
        return null;
    }

    private void sendSync(SubmitSmDto[] submitSms) {
        for (SubmitSmDto submitSm : submitSms) {
            sendOneMessage(submitSm);
        }
    }

    private void sendAsync(SubmitSmDto[] messagePack) {
        Arrays.stream(messagePack).parallel().forEach(this::sendOneMessage);

    }

    private boolean bind() {
        if (isBound) {
            return true;
        } else {
            isBound = getSessionState().isBound();
        }
        if (!isBound) {
            try {
                connectAndBind(smscAccount.getIpAddress(), smscAccount.getPort(), bindParameter);
                isBound = getSessionState().isBound();
            } catch (IOException e) {
                LOGGER.error(String.format("IOException on bind. SmscAccountId: %s; ", smscAccount.getSmscAccountId()), e);
            }
        }
        return isBound;
    }

}
