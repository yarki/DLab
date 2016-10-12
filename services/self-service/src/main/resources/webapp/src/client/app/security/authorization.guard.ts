import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { UserProfileService } from './userProfile.service'

@Injectable()
export class AuthorizationGuard implements CanActivate {
  constructor(private userProfileService : UserProfileService, private router: Router) {}

  canActivate() {
    if(!this.userProfileService.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
    return  true;
  }
}
