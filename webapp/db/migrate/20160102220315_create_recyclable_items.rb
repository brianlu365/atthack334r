class CreateRecyclableItems < ActiveRecord::Migration[5.0]
  def change
    create_table :recyclable_items do |t|
      t.string :barcode
      t.integer :points
      t.float :weight
      t.string :name

      t.timestamps
    end
  end
end
