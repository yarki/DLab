/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { Injectable } from '@angular/core';
import {Response} from '@angular/http';
import {Observable} from "rxjs";
import {ApplicationServiceFacade} from "./applicationServiceFacade.service";
import {AppRoutingService} from "../routing/appRouting.service";

@Injectable()
export class ApplicationSecurityService {
  private accessTokenKey : string = "access_token";
  private userNameKey : string = "user_name";

  constructor(private serviceFacade : ApplicationServiceFacade, private appRoutingService : AppRoutingService) {
  }

  public login(userName, password) : Observable<boolean> {
    let body = JSON.stringify({'username': userName, 'password': password, 'access_token': ''});

    return this.serviceFacade.buildLoginRequest(body).map((response : Response) => {
      if (response.status === 200) {
        this.setAuthToken(response.text());
        this.setUserName(userName);

        return true;
      }
      return false;
    }, this);
  }

  public logout() : Observable<boolean> {
    let authToken = this.getAuthToken();

    if(!!authToken)
    {
      this.clearAuthToken();
      return this.serviceFacade.buildLogoutRequest("").map((response: Response) => {
        return response.status === 200;
      }, this)
    }

    return Observable.of(false);
  }

  public getCurrentUserName() : string {
    return localStorage.getItem(this.userNameKey);
  }

  public getAuthToken() : string {
    return localStorage.getItem(this.accessTokenKey);
  }

  public isLoggedIn() : Observable<boolean> {
    let authToken = this.getAuthToken();
    let currentUser = this.getCurrentUserName();

    if(authToken && currentUser)
    {
      return this.serviceFacade.buildAuthorizeRequest(currentUser).map((response : Response) => {
        if(response.status === 200)
          return true;

        this.clearAuthToken();
        this.appRoutingService.redirectToLoginPage();
        return false;
      }, this);
    }

    this.appRoutingService.redirectToLoginPage();
    return Observable.of(false);
  }

  private setUserName(userName)
  {
    localStorage.setItem(this.userNameKey, userName);
  }

  private setAuthToken(accessToken){
    let encodedToken = accessToken;
    localStorage.setItem(this.accessTokenKey, encodedToken);
  }

  private clearAuthToken(){
    localStorage.removeItem(this.accessTokenKey);
  }
}
