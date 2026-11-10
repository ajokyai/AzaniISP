package model;

public class ContactPerson {
    private int contactId;
    private int institutionId;
    private String fullName;
    private String phone;
    private String email;
    private String position;

    public ContactPerson(String fullName, String phone, String email, String position) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.position = position;
    }

    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public int getInstitutionId() { return institutionId; }
    public void setInstitutionId(int institutionId) { this.institutionId = institutionId; }

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPosition() { return position; }
}