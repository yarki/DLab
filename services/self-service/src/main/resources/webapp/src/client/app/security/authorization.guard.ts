import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { AuthenticationService } from './../security/authentication.service';

@Injectable()
export class AuthorizationGuard implements CanActivate {
  constructor(private authenticationService: AuthenticationService, private router: Router) {}

  canActivate() {
    if(!this.authenticationService.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
    return  true;
  }
}
