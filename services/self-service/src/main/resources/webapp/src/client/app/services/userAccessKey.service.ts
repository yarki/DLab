import { Injectable } from '@angular/core';
import {Http, Response} from '@angular/http';
import { WebRequestHelper } from './../util/webRequestHelper.service'
import { UserProfileService } from './../security/userProfile.service'

@Injectable()
export class UserAccessKeyService {
  constructor(private http: Http, private webRequestHelper : WebRequestHelper,
              private userProfileService : UserProfileService) {
  }

  checkUserAccessKey() {
    let requestHeader = this.webRequestHelper.getJsonHeader();
    requestHeader = this.userProfileService.appendBasicAuthHeader(requestHeader);

    return this.http.get(`/api/keyloader?access_token=${this.userProfileService.getAuthToken()}`
      ,{headers : requestHeader}).map(( res:Response ) => res.json());
  }
}
