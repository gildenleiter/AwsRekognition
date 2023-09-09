import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InferenceComponent } from './inference.component';

describe('InferenceComponent', () => {
  let component: InferenceComponent;
  let fixture: ComponentFixture<InferenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InferenceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InferenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
