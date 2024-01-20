package com.example.e_library;

public class DeleteApiResponse {
    private final boolean Deletion;

    public DeleteApiResponse(boolean deletion) {
        Deletion = deletion;
    }

    public boolean isDeletion() {
        return Deletion;
    }

}
