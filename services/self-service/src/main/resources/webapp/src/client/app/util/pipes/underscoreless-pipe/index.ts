import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UnderscorelessPipe } from './underscoreless.pipe';

@NgModule({
  imports: [CommonModule],
  declarations: [UnderscorelessPipe],
  exports: [UnderscorelessPipe]
})

export class UnderscorelessPipeModule { }
