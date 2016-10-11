import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { APP_BASE_HREF } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { HttpModule } from '@angular/http';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

import {
  LocationStrategy,
  HashLocationStrategy
} from '@angular/common';

import { UserService } from './user.service';
import { LoggedInGuard } from './logged-in.guard';
import { LoginModule } from './login/login.module';
import { HomeModule } from './home/home.module';
import { SharedModule } from './shared/shared.module';

@NgModule({
  imports: [BrowserModule, HttpModule, RouterModule.forRoot(routes, { useHash: true }), LoginModule, HomeModule, SharedModule.forRoot()],
  declarations: [AppComponent],
  providers: [{
    provide: LocationStrategy,
    useClass: HashLocationStrategy,
    useValue: '<%= APP_BASE %>'
  }, UserService, LoggedInGuard],
  bootstrap: [AppComponent]

})

export class AppModule {}
