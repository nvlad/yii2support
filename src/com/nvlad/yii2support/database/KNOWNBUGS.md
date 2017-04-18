**code completion in with does not work**  
Person::find()->where(['id' => $id])->with('sentences.document', 'case')  
**incorrect hasMany codecompletion in case viaTable**  
return $this->hasMany(BvPerson::className(), ['id' => 'person_id'])
            ->viaTable('bv_company_person', ['company_id' => 'id']);  
**Query::from does not detected**  
$query->from('scoring');  
**Code completion incorrectly works for relations**  
$model->getRelationName()->andWhere('<caret>')  

           