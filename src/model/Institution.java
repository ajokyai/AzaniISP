package model;

public class Institution {
    private int institutionId;
    private String name;
    private String category;
    private String location;

    public Institution(String name, String category, String location) {
        this.name = name;
        this.category = category;
        this.location = location;
    }

    public int getInstitutionId() { return institutionId; }
    public void setInstitutionId(int institutionId) { this.institutionId = institutionId; }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }

    
    @Override
public String toString() {
    return name;
}
}