package com.emotion.ecm.smpp;

import com.emotion.ecm.exception.SendingException;
import com.emotion.ecm.model.SmscAccount;
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
import java.util.Arrays;

public class SmppConnection extends SMPPSession implements MessageReceiverListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmppConnection.class);

    private static final String PDU_EXCEPTION_CAPTION = "PduException message from Address: %s to Address: %s ;";
    private static final String RESPONSE_TIMEOUT_CAPTION = "ResponseTimeout from Address: %s to Address: %s ;";
    private static final String INVALID_RESPONSE_CAPTION = "InvalidResponse from Address: %s to Address: %s ;";
    private static final String NEGATIVE_RESPONSE_CAPTION = "NegativeResponse from Address: %s to Address: %s ;";
    private static final String IO_EXCEPTION_CAPTION = "IOException from Address: %s to Address: %s ;";

    @Getter
    private final SmscAccount smscAccount;
    private final BindParameter bindParameter;

    private boolean isAsync = true;
    private boolean isShuttedDown;
    private ReceivedListener<DeliverSm> dlrReceivedListener;


    public SmppConnection(SmscAccount smscAccount) {
        //TODO: 03-01-2018 Verify if "no sync" is working properly.
        //super(new DefaultPDUSender(new DefaultComposer()), new DefaultPDUReader(), SocketConnectionFactory.getInstance());
        super();
        this.smscAccount = smscAccount;
        bindParameter = new BindParameter(BindType.BIND_TRX, smscAccount.getSystemId(), smscAccount.getPassword(),
                "ecm", TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "");

        setMessageReceiverListener(this);
        isShuttedDown = true;
    }

    private boolean checkConnection() {
        if (!isShuttedDown && !getSessionState().isBound()) {
            try {
                connectAndBind(smscAccount.getIpAddress(), smscAccount.getPort(), bindParameter);
                return true;
            } catch (IOException e) {
                LOGGER.error(String.format("IOException on bind. SmscAccountId: %s; ", smscAccount.getId()), e);
            }
        }
        return false;
    }

    public void sendBulkMessages(SubmitSmDto[] submitSms) throws SendingException {
        if (checkConnection()) {
            if (isAsync) {
                sendAsync(submitSms);
            } else {
                sendSync(submitSms);
            }
            return;
        }
        throw new SendingException("Connection is shutdown.");
    }

    private void sendSync(SubmitSmDto[] submitSms) {
        for (SubmitSmDto submitSm : submitSms) {
            sendOneMessage(submitSm);
        }
    }

    private void sendAsync(SubmitSmDto[] submitSms) {
        Arrays.stream(submitSms).parallel().forEach(this::sendOneMessage);
    }

    public void sendOneMessage(SubmitSmDto submitSm) {
        try {
            String messageId = submitShortMessage(
                    submitSm.getServiceType(),
                    submitSm.getSourceTon(),
                    submitSm.getSourceNpi(),
                    submitSm.getSourceNumber(),
                    submitSm.getDestinationTon(),
                    submitSm.getDestinationNpi(),
                    submitSm.getDestinationNumber(),
                    submitSm.getEsmClass(),
                    submitSm.getProtocolId(),
                    submitSm.getPriorityFlag(),
                    submitSm.getScheduleDeliveryTime(),
                    submitSm.getValidityPeriod(),
                    submitSm.getRegisteredDelivery(),
                    submitSm.getReplaceIfPresentFlag(),
                    submitSm.getDataCoding(),
                    submitSm.getSmDefaultMsgId(),
                    submitSm.getShortMessage(),
                    submitSm.getOptionalParameters()
            );

            submitSm.setMessageId(messageId);
        } catch (PDUException e) {
            LOGGER.warn(String.format(PDU_EXCEPTION_CAPTION, submitSm.getSourceNumber(), submitSm.getDestinationNumber()), e);
        } catch (ResponseTimeoutException e) {
            LOGGER.warn(String.format(RESPONSE_TIMEOUT_CAPTION, submitSm.getSourceNumber(), submitSm.getDestinationNumber()), e);
        } catch (InvalidResponseException e) {
            LOGGER.warn(String.format(INVALID_RESPONSE_CAPTION, submitSm.getSourceNumber(), submitSm.getDestinationNumber()), e);
        } catch (NegativeResponseException e) {
            LOGGER.warn(String.format(NEGATIVE_RESPONSE_CAPTION, submitSm.getSourceNumber(), submitSm.getDestinationNumber()), e);
        } catch (IOException e) {
            LOGGER.warn(String.format(IO_EXCEPTION_CAPTION, submitSm.getSourceNumber(), submitSm.getDestinationNumber()), e);
        }
    }

    public void shutdownConnection() {
        isShuttedDown = true;
        try {
            sendOutbind(smscAccount.getSystemId(), smscAccount.getPassword());
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
}
