package com.example.e_library;

public class DeleteApiResponse {
    private  boolean Deletion;

    public DeleteApiResponse(boolean deletion) {
        Deletion = deletion;
    }

    public boolean isDeletion() {
        return Deletion;
    }

}
