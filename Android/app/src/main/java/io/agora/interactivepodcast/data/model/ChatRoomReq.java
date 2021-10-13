package io.agora.interactivepodcast.data.model;

public class ChatRoomReq extends CommonReq{
    private String name;
    private String description;
    private String maxusers;
    private String owner;
    private String[] members;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaxusers() {
        return maxusers;
    }

    public void setMaxusers(String maxusers) {
        this.maxusers = maxusers;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }
}
