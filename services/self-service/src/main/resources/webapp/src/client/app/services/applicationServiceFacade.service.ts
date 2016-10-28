/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Response, RequestOptions, RequestMethod, Headers} from '@angular/http';
import {Observable} from "rxjs";
import {Dictionary} from "../util/collections/dictionary/dictionary";

@Injectable()
export class ApplicationServiceFacade {

  private static readonly LOGIN = "login";
  private static readonly LOGOUT = "logout";
  private static readonly AUTHORIZE = "authorize";
  private static readonly ACCESS_KEY = "access_key";
  private static readonly EXPLORATORY_ENVIRONMENT = "exploratory_environment";
  private static readonly COMPUTATIONAL_RESOURCES = "computational_resources";
  private static readonly COMPUTATIONAL_RESOURCES_SHAPES = "computational_resources_shapes";
  private accessTokenKey : string = "access_token";
  private requestRegistry : Dictionary<string>;

  constructor(private http: Http) {
    this.setupRegistry();
  }

  private setupRegistry () : void {
    this.requestRegistry = new Dictionary<string>();

    // Security

    this.requestRegistry.Add(ApplicationServiceFacade.LOGIN, "/api/user/login");
    this.requestRegistry.Add(ApplicationServiceFacade.LOGOUT, "/api/user/logout");
    this.requestRegistry.Add(ApplicationServiceFacade.AUTHORIZE, "/api/user/authorize");
    this.requestRegistry.Add(ApplicationServiceFacade.ACCESS_KEY, "/api/user/access_key");

    // Exploratory Environment

    this.requestRegistry.Add(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT, "/api/infrastructure_provision/exploratory_environment");

    // Computational Resources
    this.requestRegistry.Add(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES, "/api/infrastructure_provision/computational_resources");
    this.requestRegistry.Add(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES_SHAPES, "/api/infrastructure_provision/computational_resources_shapes");
  }

  private buildRequest(method : RequestMethod, url : string, body : any, opt : RequestOptions) : Observable<Response> {
    if(method == RequestMethod.Post)
      return this.http.post(url, body, opt);
      else if (method == RequestMethod.Delete)
        return this.http.delete(url, opt);
      else if(method == RequestMethod.Put)
        return this.http.put(url, body, opt);
    else return this.http.get(url, opt);
  }

  private getJsonRequestOptions() : RequestOptions
  {
    let headers = new Headers();
    headers.append('Content-type', 'application/json; charset=utf-8');
    let reqOpt = new RequestOptions({ headers: headers });
    return reqOpt;
  }

  private getAuthRequestOptions() : RequestOptions {
    let headers = new Headers();
    headers.append('Content-type', 'application/json; charset=utf-8');
    headers.append("Authorization", "Bearer " + localStorage.getItem(this.accessTokenKey));
    let reqOpt = new RequestOptions({ headers: headers });
    return reqOpt;
  }

  buildLoginRequest(body: any) : Observable<Response> {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.LOGIN),
      body,
      this.getJsonRequestOptions());
  }

  buildLogoutRequest(body: any) : Observable<Response>  {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.LOGOUT),
      body,
      this.getAuthRequestOptions());
  }

  buildAuthorizeRequest(body:any) : Observable<Response>  {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.AUTHORIZE),
      body,
      this.getAuthRequestOptions());
  }
}
