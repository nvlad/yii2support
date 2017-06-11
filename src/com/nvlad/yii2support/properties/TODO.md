**Properties not generated for relations**  
```php
 public function getSpec() {
        return $this->hasMany(DoctorSpec::className(), ['id' => 'doctorspec_id'])->andOnCondition(['address'] )
            ->viaTable("doctor_doctorspec", ['doctor_id' => 'id']);
    }
```
** Generate properties for getters and setters**  

