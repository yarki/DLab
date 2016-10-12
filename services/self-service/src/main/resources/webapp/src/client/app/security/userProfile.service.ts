import { Injectable } from '@angular/core';

@Injectable()
export class UserProfileService {
  private accessTokenKey : string = "access_token";

  constructor() {
  }

  setUserName(userName)
  {
    localStorage.setItem('user_name', userName);
  }

  getUserName(){
    localStorage.getItem('user_name');
  }

  setAuthToken(accessToken){
    localStorage.setItem(this.accessTokenKey, accessToken);
  }

  getAuthToken() {
    return localStorage.getItem(this.accessTokenKey);
  }

  isLoggedIn() {
    return !!this.getAuthToken();
  }
}
