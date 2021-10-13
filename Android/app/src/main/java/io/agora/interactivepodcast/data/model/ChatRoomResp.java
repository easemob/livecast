package io.agora.interactivepodcast.data.model;

public class ChatRoomResp extends CommonResp{
    /**
     * {
     *   "action": "post",
     *   "application": "8be024f0-e978-11e8-b697-5d598d5f8402",
     *   "uri": "http://a1.easemob.com/easemob-demo/testapp/chatrooms",
     *   "entities": [],
     *   "data": {
     *     "id": "66213271109633"
     *   },
     *   "timestamp": 1542544296075,
     *   "duration": 0,
     *   "organization": "easemob-demo",
     *   "applicationName": "testapp"
     * }
     */

    private String organization;
    private String applicationName;
    private Data data;

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data{
       public String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
