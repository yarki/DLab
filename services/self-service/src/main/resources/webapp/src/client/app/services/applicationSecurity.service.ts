/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Response} from '@angular/http';
import {Observable} from "rxjs";
import {ApplicationServiceFacade} from "./applicationServiceFacade.service";
import {AppRoutingService} from "../routing/appRouting.service";
import {LoginModel} from "../login/loginModel";
import HTTP_STATUS_CODES from 'http-status-enum';

@Injectable()
export class ApplicationSecurityService {
  private accessTokenKey : string = "access_token";
  private userNameKey : string = "user_name";

  constructor(private serviceFacade : ApplicationServiceFacade, private appRoutingService : AppRoutingService) {
  }

  public login(loginModel : LoginModel) : Observable<boolean> {
    return this.serviceFacade
      .buildLoginRequest(loginModel.toJsonString())
      .map((response : Response) => {
      if (response.status === HTTP_STATUS_CODES.OK) {
        this.setAuthToken(response.text());
        this.setUserName(loginModel.username);

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
      return this.serviceFacade
        .buildLogoutRequest()
        .map((response: Response) => {
        return response.status === HTTP_STATUS_CODES.OK;
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
      return this.serviceFacade
        .buildAuthorizeRequest(currentUser)
        .map((response : Response) => {
        if(response.status === HTTP_STATUS_CODES.OK)
          return true;

        this.clearAuthToken();
        this.appRoutingService.redirectToLoginPage();
        return false;
      }, this);
    }

    this.appRoutingService.redirectToLoginPage();
    return Observable.of(false);
  }

  private setUserName(userName) : void
  {
    localStorage.setItem(this.userNameKey, userName);
  }

  private setAuthToken(accessToken) : void {
    let encodedToken = accessToken;
    localStorage.setItem(this.accessTokenKey, encodedToken);
  }

  private clearAuthToken() : void {
    localStorage.removeItem(this.accessTokenKey);
  }
}
