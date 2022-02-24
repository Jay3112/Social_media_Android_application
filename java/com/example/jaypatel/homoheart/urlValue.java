package com.example.jaypatel.homoheart;

public class urlValue {

    private String IP = "192.168.43.86:82";

    public String loginUrl = "http://"+IP+"/dashboard/HomoHeart/v1/userLogin.php";
    public String registerUrl = "http://"+IP+"/dashboard/HomoHeart/v1/registerUser.php";
    public String sendOtpUrl = "http://"+IP+"/dashboard/HomoHeart/v1/sendOtpToUser.php";
    public String checkOtpUrl = "http://"+IP+"/dashboard/HomoHeart/v1/verifyUserByOtp.php";
    public String profileCheckUrl = "http://"+IP+"/dashboard/HomoHeart/v1/profileCheck.php";
    public String profileDataUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getProfileData.php";
    public String updateProfileDataUrl = "http://"+IP+"/dashboard/HomoHeart/v1/updateProfileData.php";
    public String setProfileImageUrl = "http://"+IP+"/dashboard/HomoHeart/v1/uploadProfImage.php";
    public String setProfileAllImageUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getAllProfImages.php";
    public String addNewEventUrl = "http://"+IP+"/dashboard/HomoHeart/v1/addEvent.php";
    public String getEventDataUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getAllEvents.php";
    public String updateNotificaitionUrl = "http://"+IP+"/dashboard/HomoHeart/v1/addNotification.php";
    public String getNotificaitionUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getNotifications.php";
    public String getFriendsUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getFriends.php";
    public String getFrndStatusUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getFrndStatus.php";
    public String getLikeDislikelistUrl = "http://"+IP+"/dashboard/HomoHeart/v1/getLikeDislikelist.php";

    public String profileImageUrl = "http://"+IP+"/dashboard/HomoHeart/Includes/profileUploads";
    public String eventImageUrl = "http://"+IP+"/dashboard/HomoHeart/Includes/eventUploads";

    public String getLoginUrl() {
        return loginUrl;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public String getSendOtpUrl() {
        return sendOtpUrl;
    }

    public String getCheckOtpUrl(){
        return checkOtpUrl;
    }

    public String profileCheckUrl(){
        return profileCheckUrl;
    }

    public String getProfileDataUrl(){
        return profileDataUrl;
    }

    public String updateProfileDataUrl(){
        return updateProfileDataUrl;
    }

    public String getProfImage(){
        return profileImageUrl;
    }

    public String setProfileImage()
    {
        return setProfileImageUrl;
    }

    public String getProfileAllImages(){
        return setProfileAllImageUrl;
    }

    public String addNewEvent(){
        return addNewEventUrl;
    }

    public String geteventImages()
    {
        return eventImageUrl;
    }

    public String getHomeDataUrl()
    {
        return getEventDataUrl;
    }

    public String updateNotification(){
        return  updateNotificaitionUrl;
    }

    public String getNotifications(){
        return getNotificaitionUrl;
    }

    public String getFriends(){ return getFriendsUrl; }

    public String getFrndStatus(){ return getFrndStatusUrl; }

    public String getLikeDislikelist(){ return getLikeDislikelistUrl; }
}
