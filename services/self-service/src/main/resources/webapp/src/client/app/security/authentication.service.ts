import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';

@Injectable()
export class AuthenticationService {
  private accessTokenKey : string = "access_token";

  constructor(private http: Http) {
  }

  login(userName, password) {

    let jsonHeader = this.getJsonHeader();

    return this.http
      .post(
        '/api/login', JSON.stringify({'username': userName, 'password': password, accessTokenKey: ''}), { headers: jsonHeader }
      )
      .map((res) => {
        if (res.status == 200) {

          this.setAuthToken(res.text())
          this.setUserName(userName);
          return true;
        }
        return false;
      }, this);
  }

  logout() {
    let jsonHeader = this.getJsonHeader();

    let authToken = this.getAuthToken();

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

  getJsonHeader() : Headers {
    let result = new Headers();
    result.append('Content-type', 'application/json; charset=utf-8');
    return result;
  }

}
