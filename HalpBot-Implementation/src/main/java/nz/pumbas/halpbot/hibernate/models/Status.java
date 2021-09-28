package nz.pumbas.halpbot.hibernate.models;

public enum Status
{
    CONFIRMED(true),
    ADDED,
    EDITED;

    private boolean isConfirmed;

    Status() {
        this.isConfirmed = false;
    }

    Status(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public boolean isConfirmed() {
        return this.isConfirmed;
    }
}
