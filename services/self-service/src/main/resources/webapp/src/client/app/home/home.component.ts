import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from './../security/authentication.service';


@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css'],
})


export class HomeComponent implements OnInit {
  constructor(private authenticationService: AuthenticationService, private router: Router) {}

   logout() {
     this.authenticationService.logout();
     this.router.navigate(['/login']);
   }

  ngOnInit() {
  }
}
