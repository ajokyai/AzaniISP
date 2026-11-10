package model;

import java.time.LocalDate;

public class Survey {
    private int surveyId;
    private int institutionId;
    private int numUsers;
    private int pcsExisting;
    private int pcsToBuy;
    private boolean lanReady;
    private int nodesToBuy;
    private LocalDate surveyDate;

    public Survey(int institutionId, int numUsers, int pcsExisting,
                      int pcsToBuy, boolean lanReady, int nodesToBuy, LocalDate surveyDate) {
        this.institutionId = institutionId;
        this.numUsers = numUsers;
        this.pcsExisting = pcsExisting;
        this.pcsToBuy = pcsToBuy;
        this.lanReady = lanReady;
        this.nodesToBuy = nodesToBuy;
        this.surveyDate = surveyDate;
    }

    public int getSurveyId() { return surveyId; }
    public void setSurveyId(int surveyId) { this.surveyId = surveyId; }
    public int getInstitutionId() { return institutionId; }
    public int getNumUsers() { return numUsers; }
    public int getPcsExisting() { return pcsExisting; }
    public int getPcsToBuy() { return pcsToBuy; }
    public boolean isLanReady() { return lanReady; }
    public int getNodesToBuy() { return nodesToBuy; }
    public LocalDate getSurveyDate() { return surveyDate; }
}