import { Injectable } from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import { UserProfileService } from './userProfile.service'

@Injectable()
export class AuthorizationGuard implements CanActivate {
  constructor(private userProfileService : UserProfileService, private router: Router) {}

  canActivate(next:ActivatedRouteSnapshot, state:RouterStateSnapshot)  {
    return this.userProfileService.isLoggedIn();
  }
}
