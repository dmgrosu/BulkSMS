package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsMessageDao;
import com.emotion.ecm.dao.SmsPreviewDao;
import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.exception.ReportException;
import com.emotion.ecm.model.dto.report.MessageCount;
import com.emotion.ecm.model.dto.report.ReportGeneral;
import com.emotion.ecm.model.dto.report.ReportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Set;

@Service
public class ReportService {

    private SmsMessageDao smsMessageDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    public ReportService(SmsMessageDao smsMessageDao) {
        this.smsMessageDao = smsMessageDao;
    }

    public ReportGeneral generateGeneralReport(ReportRequest reportRequest) throws ReportException {
        if (reportRequest == null) {
            throw new ReportException("Report request object is null");
        }
        ReportGeneral result = new ReportGeneral();
        result.setAccountName(reportRequest.getAccountName());
        result.setUsername(reportRequest.getUsername());
        LocalDateTime startDate = reportRequest.getStartDate().atStartOfDay();
        LocalDateTime endDate = reportRequest.getEndDate().atTime(23, 59, 59);
        endDate = endDate.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : endDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);
        String period = startDate.format(formatter) + " - " + endDate.format(formatter);
        result.setPeriod(period);
        List<MessageCount> countByStatus = generateCountsByStatus(reportRequest, startDate, endDate);
        Set<String> reportDetails = reportRequest.getDetails();
        try {
            for (String reportDetail : reportDetails) {
                switch (reportDetail) {
                    case "status":
                        result.setCountByStatus(countByStatus);
                        break;
                    case "prefix":
                        result.setCountByPrefix(generateCountsByPrefixes(reportRequest, startDate, endDate));
                        break;
                    case "smppAddr":
                        result.setCountBySmppAddress(generateCountsBySmppAddr(reportRequest, startDate, endDate));
                        break;
                    case "preview":
                        result.setCountByPreview(generateCountsByPreview(reportRequest, startDate, endDate));
                        break;
                    default:
                        String errorStr = String.format("Unsupported general report details: %s", reportDetail);
                        LOGGER.error(errorStr);
                        throw new ReportException(errorStr);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new ReportException("Dao layer exception in generateGeneralReport");
        }

        long[] totals = getTotals(countByStatus);

        result.setTotalCount(totals[0]);
        result.setTotalSentCount(totals[1]);
        result.setTotalNotSentCount(totals[2]);

        return result;
    }

    private long[] getTotals(List<MessageCount> countList) {
        long[] result = new long[3];
        if (countList == null) {
            return result;
        }
        try {
            for (MessageCount messageCount : countList) {
                result[0] += messageCount.getCount(); // total created
                if (MessageStatus.valueOf(messageCount.getCriteriaName()).isSent()) {
                    result[1] += messageCount.getCount(); // total sent
                } else {
                    result[2] += messageCount.getCount(); // total not sent
                }
            }
        } catch (Exception ex) {
            LOGGER.error("error in getTotals", ex);
            return result;
        }
        return result;
    }

    private List<MessageCount> generateCountsByStatus(ReportRequest reportRequest, LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        List<MessageCount> result;
        Integer accountId = reportRequest.getAccountId();
        Integer userId = reportRequest.getUserId();
        if (accountId == null) {
            result = smsMessageDao.getMessageCountByStatus(startDate, endDate);
        } else if (userId == null) {
            result = smsMessageDao.getMessageCountByStatusByAccount(accountId, startDate, endDate);
        } else {
            result = smsMessageDao.getMessageCountByStatusByUser(userId, startDate, endDate);
        }
        return result;
    }

    private List<MessageCount> generateCountsByPrefixes(ReportRequest reportRequest, LocalDateTime startDate,
                                                        LocalDateTime endDate) {
        List<MessageCount> result;
        Integer accountId = reportRequest.getAccountId();
        Integer userId = reportRequest.getUserId();
        if (accountId == null) {
            result = smsMessageDao.getMessageCountByPrefixes(startDate, endDate);
        } else if (userId == null) {
            result = smsMessageDao.getMessageCountByPrefixesByAccount(accountId, startDate, endDate);
        } else {
            result = smsMessageDao.getMessageCountByPrefixesByUser(userId, startDate, endDate);
        }
        return result;
    }

    private List<MessageCount> generateCountsBySmppAddr(ReportRequest reportRequest, LocalDateTime startDate,
                                                        LocalDateTime endDate) {
        List<MessageCount> result;
        Integer accountId = reportRequest.getAccountId();
        Integer userId = reportRequest.getUserId();
        if (accountId == null) {
            result = smsMessageDao.getMessageCountBySmppAddress(startDate, endDate);
        } else if (userId == null) {
            result = smsMessageDao.getMessageCountBySmppAddressByAccount(accountId, startDate, endDate);
        } else {
            result = smsMessageDao.getMessageCountBySmppAddressByUser(userId, startDate, endDate);
        }
        return result;
    }

    private List<MessageCount> generateCountsByPreview(ReportRequest reportRequest, LocalDateTime startDate,
                                                       LocalDateTime endDate) {
        List<MessageCount> result;
        Integer accountId = reportRequest.getAccountId();
        Integer userId = reportRequest.getUserId();
        if (accountId == null) {
            result = smsMessageDao.getMessageCountByPreview(startDate, endDate);
        } else if (userId == null) {
            result = smsMessageDao.getMessageCountByPreviewByAccount(accountId, startDate, endDate);
        } else {
            result = smsMessageDao.getMessageCountByPreviewByUser(userId, startDate, endDate);
        }
        return result;
    }

}
