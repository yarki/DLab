import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import {Observable} from "rxjs";
import {AppRoutingService} from "../routing/appRouting.service";

@Injectable()
export class UserProfileService {
  private accessTokenKey : string = "access_token";
  private userNameKey : string = "user_name";
  constructor(private http: Http, private webRequestHelper : WebRequestHelper, private appRoutingService : AppRoutingService) {}

  setUserName(userName)
  {
    localStorage.setItem(this.userNameKey, userName);
  }

  getCurrentUserName() : string {
    return localStorage.getItem(this.userNameKey);
  }

  setAuthToken(accessToken){
    let encodedToken = accessToken;
    localStorage.setItem(this.accessTokenKey, encodedToken);
  }

  clearAuthToken(){
    localStorage.removeItem(this.accessTokenKey);
  }

  getAuthToken() : string {
    return localStorage.getItem(this.accessTokenKey);
  }

  appendBasicAuthHeader (header : Headers) : Headers
  {
    let authToken = this.getAuthToken();
    header.append("Authorization", "Bearer " + authToken);
    return header;
  }

  isLoggedIn() : Observable<boolean> {
    let authToken = this.getAuthToken();
    let currentUser = this.getCurrentUserName();

    if(authToken && currentUser)
    {
      let requestHeader = this.webRequestHelper.getJsonHeader();
      requestHeader = this.appendBasicAuthHeader(requestHeader);

      return this.http
        .post(
          '/api/authorize',
          currentUser,
          { headers: requestHeader }
        )
        .map((response) => {
            if(response && response.status == 200)
            {
              return true;
            }

          this.appRoutingService.redirectToLoginPage();
          return false;
        }, this);
    }

    this.appRoutingService.redirectToLoginPage();
    return Observable.of(false);
  }
}
