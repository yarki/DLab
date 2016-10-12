import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthenticationService } from './../security/authentication.service';

/**
 * This class represents the lazy loaded LoginComponent.
 */
@Component({
  moduleId: module.id,
  selector: 'sd-login',
  templateUrl: 'login.component.html',
  styleUrls: ['login.component.css'],
  providers: [AuthenticationService]
})
export class LoginComponent {

  constructor(private authenticationService: AuthenticationService, private router: Router) {}

	onSubmit(username, password) {
    this.authenticationService.login(username, password).subscribe((result) => {
      if (result) {
        this.router.navigate(['/dashboard']);
        return true;
      }

      return false;
    });

  }
}
