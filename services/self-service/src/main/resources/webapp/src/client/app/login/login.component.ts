import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthenticationService } from './../security/authentication.service';
import {UserProfileService} from "../security/userProfile.service";

@Component({
  moduleId: module.id,
  selector: 'sd-login',
  templateUrl: 'login.component.html',
  styleUrls: ['login.component.css'],
  providers: [AuthenticationService, UserProfileService]
})
export class LoginComponent {

  constructor(private authenticationService: AuthenticationService, private userProfileService : UserProfileService, private router: Router) {}

	onSubmit(username, password) {
    this.authenticationService
      .login(username, password)
      .subscribe((result) => {
        if (result) {
          this.router.navigate(['/dashboard']);
          return true;
        }

        return false;
    });
  }

  ngOnInit() {
    if(this.userProfileService.isLoggedIn())
      this.router.navigate(['/dashboard']);
  }
}
