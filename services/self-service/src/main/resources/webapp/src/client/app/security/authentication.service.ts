import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import { UserProfileService } from './userProfile.service'
 import {Observable} from "rxjs";

@Injectable()
export class AuthenticationService {
  constructor(private http: Http, private webRequestHelper : WebRequestHelper,
              private userProfileService : UserProfileService) {
  }

  login(userName, password) : Observable<Boolean> {

    let requestHeader = this.webRequestHelper.getJsonHeader();

    return this.http
      .post(
        '/api/login', JSON.stringify({'username': userName, 'password': password, 'access_token': ''}), { headers: requestHeader }
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

  logout() : Observable<String> {
    let requestHeader = this.webRequestHelper.getJsonHeader();
    let authToken = this.userProfileService.getAuthToken();

    if(!!authToken)
    {
      requestHeader = this.userProfileService.appendBasicAuthHeader(requestHeader);

      this.userProfileService.clearAuthToken();
      return this.http
        .post(
          '/api/logout',
          JSON.stringify({accessTokenKey: authToken}),
          { headers: requestHeader }
      ).map(res => res.text());
    }
  }
}
