/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import {ApplicationSecurityService} from "./applicationSecurity.service";

@Injectable()
export class UserResourceService {
  url = {
    createTmpls: 'userlist/exploratory',
    emrTmpls: 'userlist/computational',
    shapes: 'userlist/shape',
    createNotebook: 'exploratory/create',
    startNotebook: 'exploratory/start',
    stopNotebook: 'exploratory/stop',
    terminateNotebook: 'exploratory/terminate',
    createEmr: 'emr/create',
    terminateEmr: 'emr/terminate',
    gridData: 'userlist',
    keyloader: 'keyloader'
  };

  constructor(private applicationSecurityService: ApplicationSecurityService,
              private http : Http,
              private webRequestHelper : WebRequestHelper) {
  }

  getResourceUrl(resource: string) {
   return `/api/${this.url[resource]}?access_token=${this.applicationSecurityService.getAuthToken()}`;
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

  startUsernotebook(data) {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('startNotebook'), body, { headers: requestHeader })
        .map((res) => {
          return res;
      });
  }

  stopUsernotebook(data) {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('stopNotebook'), body, { headers: requestHeader })
        .map((res) => {
          return res;
      });
  }

  terminateUsernotebook(data) {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('terminateNotebook'), body, { headers: requestHeader })
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

  terminateEmr(data) {
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getResourceUrl('terminateEmr'), body, { headers: requestHeader })
        .map((res: Response) => {
          return res.status;
        });
  }

  uploadKey(data)
  {
    let body = data;
      return this.http.post(this.getResourceUrl('keyloader'), body)
        .map((res: Response) => {
          return res.status;
        });
  }
}
