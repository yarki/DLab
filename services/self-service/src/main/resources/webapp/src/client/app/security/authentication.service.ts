/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
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
      .map((res : Response) => {
          if (res.status == 200) {

            this.userProfileService.setAuthToken(res.text())
            this.userProfileService.setUserName(userName);
            return true;
          }
          return false;
      }, this);
  }

  logout() : Observable<Boolean> {
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
      ).map((res) => {
            if (res.status == 200) {
              return true;
            }
            return false;
          }, this);
    }

    return Observable.of(false);
  }
}
