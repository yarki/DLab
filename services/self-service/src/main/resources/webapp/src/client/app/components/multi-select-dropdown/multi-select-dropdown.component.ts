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


import { Input, Output, Component, EventEmitter, ViewChild, HostListener, ElementRef} from '@angular/core';

@Component({
  moduleId: module.id,
  selector: 'multi-select-dropdown',
  templateUrl: 'multi-select-dropdown.component.html',
  styleUrls: ['./multi-select-dropdown.component.css']
})

export class MultiSelectDropdown {


  @Input() items: Array<any>;
  @Input() model: Array<any>;
  @Output() selectionChange: EventEmitter<{}> = new EventEmitter();


  @ViewChild('wrapper') wrapper: ElementRef;
  @HostListener('window:click', ['$event'])
    clickHandler(event) {

      var parent = event.target;
      while(parent != this.wrapper.nativeElement && parent != document) {
        parent = parent.parentNode;
      }
      if(parent == document) {
        this.toggleDropdown();
      }
    }

  isOpen: boolean = false;

  toggleDropdown() : void {
    this.isOpen = !this.isOpen;
  }

  toggleSelectedOptions($event, model, value) {
    let index = model.indexOf(value);
    (index >= 0) ? model.splice(index, 1) : model.push(value);

    console.log(this.model);
    this.onUpdate();
    $event.preventDefault();
  }

  deselectAllOptions($event) {
    this.model = [];

    console.log(this.model);
    $event.preventDefault();
  }

  onUpdate() : void {
    this.selectionChange.emit(this.model);
  }
}
