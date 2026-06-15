package com.example.myapplication.models;

public class ActiveSessionResponse {
    private boolean success;
    private Session session;

    public ActiveSessionResponse(boolean success, Session session) {
        this.success = success;
        this.session = session;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
