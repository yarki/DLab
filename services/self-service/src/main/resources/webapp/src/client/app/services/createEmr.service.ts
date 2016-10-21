import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import { UserProfileService } from './../security/userProfile.service'

@Injectable()
export class UserEmrService {

  constructor(private http: Http,
              private webRequestHelper : WebRequestHelper,
              private userProfileService : UserProfileService) {
  }

  getEmrTemplatesUrl() : string
  {
    return `/api/docker?access_token=${this.userProfileService.getAuthToken()}`;
  }

  getCreateUsernotebookUrl() : string
  {
    return `/api/exploratory/create?access_token=${this.userProfileService.getAuthToken()}`;
  }

  createComputational (data)
  {  
    let body = JSON.stringify(data);
    let requestHeader = this.webRequestHelper.getJsonHeader();
      return this.http.post(this.getCreateUsernotebookUrl(), body, { headers: requestHeader })
        .map((res) => {
          return res;
      });
  }
}