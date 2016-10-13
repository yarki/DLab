import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'

@Injectable()
export class UserProfileService {
  private accessTokenKey : string = "access_token";

  constructor(private http: Http, private webRequestHelper : WebRequestHelper) {}

  setUserName(userName)
  {
    localStorage.setItem('user_name', userName);
  }

  getCurrentUserName() : string {
    return localStorage.getItem('user_name');
  }

  setAuthToken(accessToken){
    localStorage.setItem(this.accessTokenKey, accessToken);
  }

  getAuthToken() : string {
    return localStorage.getItem(this.accessTokenKey);
  }

  isLoggedIn() {
    let authToken = this.getAuthToken();
    let currentUser = this.getCurrentUserName();

    if(authToken && currentUser)
    {
      let jsonHeader = this.webRequestHelper.getJsonHeader();

      return this.http
        .post(
          '/api/authorize',
          JSON.stringify(
            [{'username': currentUser, 'password': '' , 'access_token': authToken},
              {'username': currentUser}]
          ),
          { headers: jsonHeader }
        )
        .map((response) => {
          if (response.status == 200) {
            return true;
          }
          return false;
        }, this);
    }
  }
}
