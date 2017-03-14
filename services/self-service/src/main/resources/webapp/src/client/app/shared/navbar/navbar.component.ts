/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { Component, ViewEncapsulation, Input, OnInit } from '@angular/core';
import { ApplicationSecurityService } from '../../services/applicationSecurity.service';
import { AppRoutingService } from '../../routing/appRouting.service';
import { HealthStatusService } from '../../services/healthStatus.service';

@Component({
  moduleId: module.id,
  selector: 'sd-navbar',
  templateUrl: 'navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  encapsulation: ViewEncapsulation.None
})

export class NavbarComponent implements OnInit {
  currentUserName: string;

  @Input() healthStatus: string;

  constructor(
    private applicationSecurityService: ApplicationSecurityService,
    private appRoutingService: AppRoutingService,
    private healthStatusService: HealthStatusService
  ) { }

  ngOnInit() {
    this.currentUserName = this.getUserName();
  }

  getUserName() {
    return this.applicationSecurityService.getCurrentUserName() || '';
  }

  logout_btnClick() {
    this.applicationSecurityService.logout()
      .subscribe(
      () => this.appRoutingService.redirectToLoginPage(),
      error => console.log(error),
      () => this.appRoutingService.redirectToLoginPage());
  }
}
