package com.revonline.pastebin;

import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 01/12/13
 * Time: 15.48
 * To change this template use File | Settings | File Templates.
 */
public class PasteInfo {
    public int sqlID;
    private String pasteName;
    private String pasteAuthor;
    private String pasteLanguage;
    private GregorianCalendar pasteData = new GregorianCalendar(); // mb .sql.Date ?
    private String pasteKey;

    public PasteInfo(String pasteName, String pasteAuthor, String pasteLanguage, GregorianCalendar pasteData, String pasteKey) {
        this.pasteName = pasteName;
        this.pasteAuthor = pasteAuthor;
        this.pasteLanguage = pasteLanguage;
        this.pasteData = pasteData;
        this.pasteKey = pasteKey;
    }

    public PasteInfo() { }

    public String getPasteName() {
        return pasteName;
    }

    public void setPasteName(String pasteName) {
        this.pasteName = pasteName;
    }

    public String getPasteAuthor() {
        return pasteAuthor;
    }

    public void setPasteAuthor(String pasteAuthor) {
        this.pasteAuthor = pasteAuthor;
    }

    public String getPasteLanguage() {
        return pasteLanguage;
    }

    public void setPasteLanguage(String pasteLanguage) {
        this.pasteLanguage = pasteLanguage;
    }

    public GregorianCalendar getPasteData() {
        return pasteData;
    }

    public void setPasteData(GregorianCalendar pasteData) {
        this.pasteData = pasteData;
    }

    public String getPasteKey() {
        return pasteKey;
    }

    public void setPasteKey(String pasteKey) {
        this.pasteKey = pasteKey;
    }

    public int getSqlID() {
        return sqlID;
    }

    public void setSqlID(int sqlID) {
        this.sqlID = sqlID;
    }
}
