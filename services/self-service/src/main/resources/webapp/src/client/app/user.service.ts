import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';

@Injectable()
export class UserService {

  constructor(private http: Http) {
  }

  login(username, password) {

    let headers = new Headers();
    headers.append('Content-type', 'application/json; charset=utf-8');

    return this.http
      .post(
        '/api/login', 
        JSON.stringify({'username': username, 'password': password, 'access_token': ''}),
        { headers }
      )
      .map((res) => {
        if (res.status == 200) {
          console.log(res.text());
          this.setAuthToken(res);
          this.setUserName(username);
          return true;
        }
        return false;
      }, this);
  }
  
  logout() {
    let headers = new Headers();
    headers.append('Content-type', 'application/json; charset=utf-8');

    let authToken = this.getAuthToken();
    if(!!authToken)
    {
    localStorage.removeItem('auth_token');
    this.http
      .post(
        '/api/logout', 
        JSON.stringify({'access_token': authToken}),
        { headers }
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
    localStorage.setItem('auth_token', accessToken);
  }

  getAuthToken() {
     return localStorage.getItem('auth_token');
  }

  isLoggedIn() {
    return !!localStorage.getItem('auth_token');
  }
}