import { Injectable } from '@angular/core';
import {Http} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import { UserProfileService } from './userProfile.service'

@Injectable()
export class AuthenticationService {
  private accessTokenKey : string = "access_token";

  constructor(private http: Http, private webRequestHelper : WebRequestHelper,
              private userProfileService : UserProfileService) {
  }

  login(userName, password) {

    let jsonHeader = this.webRequestHelper.getJsonHeader();

    return this.http
      .post(
        '/api/login', JSON.stringify({'username': userName, 'password': password, 'access_token': ''}), { headers: jsonHeader }
      )
      .map((res) => {
        if (res.status == 200) {

          this.userProfileService.setAuthToken(res.text())
          this.userProfileService.setUserName(userName);
          return true;
        }
        return false;
      }, this);
  }

  logout() {
    let jsonHeader = this.webRequestHelper.getJsonHeader();

    let authToken = this.userProfileService.getAuthToken();

    if(!!authToken)
    {
      localStorage.removeItem(this.accessTokenKey);
      this.http
        .post(
          '/api/logout',
          JSON.stringify({accessTokenKey: authToken}),
          { headers: jsonHeader }
        );
    }
  }
}
