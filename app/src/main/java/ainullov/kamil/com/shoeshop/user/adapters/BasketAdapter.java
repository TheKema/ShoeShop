package ainullov.kamil.com.shoeshop.user.adapters;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ainullov.kamil.com.shoeshop.MainActivity;
import ainullov.kamil.com.shoeshop.R;
import ainullov.kamil.com.shoeshop.db.DataBaseHelper;
import ainullov.kamil.com.shoeshop.user.fragments.ShoesDetailedFragment;
import ainullov.kamil.com.shoeshop.user.pojo.BasketFavoriteShoe;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<BasketFavoriteShoe> basketFavoriteShoes;

    private Context context;

    public BasketAdapter(Context context, List<BasketFavoriteShoe> basketFavoriteShoes) {
        this.basketFavoriteShoes = basketFavoriteShoes;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public BasketAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.basket_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BasketAdapter.ViewHolder holder, int position) {
        BasketFavoriteShoe basketFavoriteShoe = basketFavoriteShoes.get(position);

//      Получаем уникальный ключ, полученный ранее из бд basket, по которому в бд shoe находим запись
        String uniquekey = String.valueOf(basketFavoriteShoe.getUniquekey());

        Cursor c;
        DataBaseHelper dbHelper;
        String selection = null;
        String[] selectionArgs = null;
        int idColIndex;   // Если в нужно будет добавить дополнительную информацию в item
        int uniquekeyColIndex;
        int nameColIndex;
        int coastColIndex;
        int discountColIndex;
        int imageurlColIndex;

        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        selection = "uniquekey = ?";             // по уникальному ключу из basket
        selectionArgs = new String[]{uniquekey}; // в таблице shoe ищем товар и получаем необходимую информацию
        c = db.query("shoe", null, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        if (c.moveToFirst()) {
            idColIndex = c.getColumnIndex("id");
            uniquekeyColIndex = c.getColumnIndex("uniquekey");
            nameColIndex = c.getColumnIndex("name");
            coastColIndex = c.getColumnIndex("coast");
            discountColIndex = c.getColumnIndex("discount");
            imageurlColIndex = c.getColumnIndex("imageurl");

            do {

                holder.tvBasketName.setText(c.getString(nameColIndex));
                holder.tvBasketCoast.setText(String.valueOf(c.getInt(coastColIndex)));
                holder.tvBasketSize.setText(basketFavoriteShoe.getSize());

                int discountcoast = 0;
                if (c.getInt(discountColIndex) != 0 && c.getInt(discountColIndex) != 100) {
                    discountcoast = (int) (100 - c.getInt(discountColIndex)) * c.getInt(coastColIndex) / 100;

                    holder.tvBasketCoast.setPaintFlags(holder.tvBasketCoast.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvBasketCoast.setTextColor(context.getResources().getColor(R.color.red));

                    holder.tvBasketDiscountCoast.setText(String.valueOf(discountcoast));


                } else
                    holder.tvBasketDiscountCoast.setText("");

                Picasso.with(context).load(c.getString(imageurlColIndex)).into(holder.ivBasketShoe);

            } while (c.moveToNext());
        }
        c.close();
        dbHelper.close();
    }

    @Override
    public int getItemCount() {
        return basketFavoriteShoes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout clBasket;
        final ImageView ivBasketShoe;
        final TextView tvBasketName, tvBasketCoast, tvBasketDiscountCoast, tvBasketSize;
        final Button btnDelete;

        ViewHolder(View view) {
            super(view);
            clBasket = (ConstraintLayout) view.findViewById(R.id.clFavorite);
            ivBasketShoe = (ImageView) view.findViewById(R.id.ivFavoriteShoe);
            tvBasketName = (TextView) view.findViewById(R.id.tvFavoriteName);
            tvBasketCoast = (TextView) view.findViewById(R.id.tvFavoriteCoast);
            tvBasketSize = (TextView) view.findViewById(R.id.tvBasketSize);
            tvBasketDiscountCoast = (TextView) view.findViewById(R.id.tvBasketDiscountCoast);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);

            //Переход к товару из корзины, переход к ShoesDetailedFragment
            clBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BasketFavoriteShoe basketFavoriteShoe = basketFavoriteShoes.get(getAdapterPosition());

                    ShoesDetailedFragment shoesDetailedFragment = new ShoesDetailedFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("uniquekey", basketFavoriteShoe.getUniquekey());
                    shoesDetailedFragment.setArguments(bundle);

                    FragmentTransaction fTrans;
                    fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                    fTrans.replace(R.id.container, shoesDetailedFragment);
                    fTrans.addToBackStack(null);
                    fTrans.commit();
                }
            });

            // Удаление элемента в списке
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataBaseHelper dbHelper;

                    BasketFavoriteShoe basketFavoriteShoe = basketFavoriteShoes.get(getAdapterPosition());
                    int deleteItemByUniqueKey = basketFavoriteShoe.getUniquekey();
                    dbHelper = new DataBaseHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(MainActivity.USERNAME_BASKET_DB, "shoeUniquekeyBasket = " + deleteItemByUniqueKey, null);
                    dbHelper.close();

                    basketFavoriteShoes.remove(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
        }
    }
}