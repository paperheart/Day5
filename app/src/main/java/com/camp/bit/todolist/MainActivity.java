package com.camp.bit.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.camp.bit.todolist.beans.Note;
import com.camp.bit.todolist.beans.State;
import com.camp.bit.todolist.db.TodoContract;
import com.camp.bit.todolist.db.TodoDbHelper;
import com.camp.bit.todolist.debug.DebugActivity;
import com.camp.bit.todolist.ui.NoteListAdapter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    TodoDbHelper mDbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new TodoDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            String[] projection = {
                    BaseColumns._ID,
                    TodoContract.TodoEntry.MESSAGE,
                    TodoContract.TodoEntry.STATE,
                    TodoContract.TodoEntry.TIME

            };
            String selection = TodoContract.TodoEntry.MESSAGE + " = ? ";
            String[] selectionArgs = {" My Message "};

            String sortOrder = TodoContract.TodoEntry.TIME + " DESC ";
            cursor = db.query(
                    TodoContract.TodoEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );
            while (cursor.moveToNext())
            {
                String content_message = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.MESSAGE));
                long content_time = Long.parseLong(cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.TIME)));
                int state = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.STATE));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry._ID));
                Note note = new Note(id);
                note.setContent(content_message);
                note.setDate(new Date(content_time));
                note.setState(State.from(state));
                result.add(note);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return result;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据

        String selection = TodoContract.TodoEntry._ID + " LIKE ? ";
        String[]selectionArgs={String.valueOf(note.id)};
        Log.d("Text",String.valueOf(note.id));
        int deletedRows = db.delete(TodoContract.TodoEntry.TABLE_NAME,selection,selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // 更新数据
        String selection = TodoContract.TodoEntry._ID + " LIKE ? ";
        String[]selectionArgs={""+note.id};
        ContentValues values = new ContentValues();
        //Log.d("Text",""+note.getState().intValue);
        if(note.getState().intValue == 1) {
            values.put(TodoContract.TodoEntry.STATE, 1);
            Log.d("Text",""+note.getState().intValue);
        }else
            values.put(TodoContract.TodoEntry.STATE,0);
        db.update(TodoContract.TodoEntry.TABLE_NAME,values,selection,selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}
