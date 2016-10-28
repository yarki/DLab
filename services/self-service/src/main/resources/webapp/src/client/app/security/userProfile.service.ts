/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import {Observable} from "rxjs";
import {AppRoutingService} from "../routing/appRouting.service";
import {ApplicationServiceFacade} from "../services/applicationServiceFacade.service";

@Injectable()
export class UserProfileService {
  private accessTokenKey : string = "access_token";
  private userNameKey : string = "user_name";
  constructor(private serviceFacade : ApplicationServiceFacade, private appRoutingService : AppRoutingService) {}

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
}
