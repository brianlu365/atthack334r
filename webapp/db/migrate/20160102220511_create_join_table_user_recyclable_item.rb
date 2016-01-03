class CreateJoinTableUserRecyclableItem < ActiveRecord::Migration[5.0]
  def change
    create_join_table :users, :recyclable_items do |t|
      # t.index [:user_id, :recyclable_item_id]
      # t.index [:recyclable_item_id, :user_id]
    end
  end
end
