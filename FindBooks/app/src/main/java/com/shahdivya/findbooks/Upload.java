package com.shahdivya.findbooks;

public class Upload {
    public String names;
    public String url;
    public String email;
    public String key;
    //public String desp;
    public Upload(){
    }

    public Upload(String names,String url,String email,String key){
        this.names = names;
        this.url = url;
        this.email = email;
        this.key = key;
        //this.desp = desp;
    }

    public String getNames() {
        return names;
    }

    public String getUrl() {
        return url;
    }

    public String getEmail(){
        return email;
    }

    public String getKey()
    {
        return key;
    }

}
