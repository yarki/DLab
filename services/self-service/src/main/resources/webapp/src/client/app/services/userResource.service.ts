/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import { UserProfileService } from './../security/userProfile.service'
import {Headers} from "@angular/http";

@Injectable()
export class UserResourceService {
  url = {
    createTmpls: 'userlist/exploratory',
    emrTmpls: 'userlist/computational',
    shapes: 'userlist/shape',
    createNotebook: 'exploratory/create',
    createEmr: 'emr/create',
    gridData: 'userlist'
  };

  constructor(private http: Http,
              private webRequestHelper : WebRequestHelper,
              private userProfileService : UserProfileService) {
  }

  getResourceUrl(resource: string) {
   return `/api/${this.url[resource]}?access_token=${this.userProfileService.getAuthToken()}`;
  }

  getCreateTmpl()
  {
    return this.http.get(this.getResourceUrl('createTmpls'))
      .map(( res:Response ) => res.json())
      .catch((error: any) => error);
  }

  getEmrTmpl()
  {
    return this.http.get(this.getResourceUrl('emrTmpls'))
      .map(( res:Response ) => res.json())
      .catch((error: any) => error);
  }

  getShapes()
  {
    return this.http.get(this.getResourceUrl('shapes'))
      .map(( res:Response ) => res.json())
      .catch((error: any) => error);
  }

  getGridData() {
    return this.http.get(this.getResourceUrl('gridData'))
      .map(( res:Response ) => res.json())
      .catch((error: any) => error);
  }

  createUsernotebook(data)
  {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('createNotebook'), body, { headers: requestHeader })
        .map((res) => {
          return res;
      });
  }

  createEmr(data)
  {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('createEmr'), body, { headers: requestHeader })
        .map(res => res.json());
  }

  uploadKey(data)
  {
    let body = data;
    // let headers = new Headers({
    //     "enctype": "multipart/form-data"
    // });
      return this.http.post("/api/keyloader?access_token=token123", body)
        .map((res) => {
          return res;
      });
  }
}
