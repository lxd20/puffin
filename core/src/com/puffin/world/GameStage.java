package com.puffin.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.puffin.projectile.Projectile;
import com.puffin.runner.Runner;
import com.puffin.util.BodyUtils;
import com.puffin.util.Constants;
import com.puffin.util.GameActor;
import com.puffin.util.WorldUtils;

import java.util.ArrayList;

/**
 * Controls all functionality related to the stage of the game
 */

public class GameStage extends Stage implements ContactListener{

    private World world;
    private ArrayList<Ground> grounds;
    private Maps map;
    private Runner runner; //the puffin

    private final float TIME_STEP = 1 / 300f;
    private float accumulator = 0f;

    private float accumulate = 0f;
    private float accumulate2 = 0f; // Used to calibrate delay for firing

    private OrthographicCamera camera;
    private Box2DDebugRenderer renderer;
    private SpriteBatch sb;

    private Rectangle screenLeftSide;
    private Rectangle screenRightSide;

    private Vector3 touchPoint;

    public GameStage() {
        setUpWorld();
        setupCamera();
        setupTouchControlAreas();
        renderer = new Box2DDebugRenderer();
        sb = new SpriteBatch();
    }

    /**
     * Creates the world with:
     * world field is given the set gravity
     * ground is set up with setUpGround()
     * runner is set up with setUpRunner()
     */
    private void setUpWorld() {
        world = WorldUtils.createWorld();
        world.setContactListener(this);
        setUpGround();
        setUpRunner();
    }

    /**
     * Sets ground field to new body with fields specified in WorldUtils file.
     * Adds ground field to actor list
     */
    private void setUpGround() {
        testGround();
        grounds = new ArrayList<Ground>();
        map = new Maps();
        for(int i = 0; i < 10; i ++) {
            Ground ground = new Ground(map.next().createPlatform(world));
            addActor(ground);
            grounds.add(ground);
        }
    }
    /**
     * Adds new ground
     */
    private void testGround(){
        Ground ground = new Ground(map.next().createPlatform(world));
        System.out.println("Ground xPos: " + ground.getPosition().x + "Ground Height: " + ground.getHeight() + "Ground width: " + ground.getWidth());

        Ground ground2 = new Ground(map.generateNext(ground).createPlatform(world));
        System.out.println("Ground xPos: " + ground2.getPosition().x + "Ground Height: " + ground2.getHeight() + "Ground width: " + ground2.getWidth());

        for (int i = 0; i < 5; i++) {
            ground = new Ground(map.generateNext(ground2).createPlatform(world));
            System.out.println("Ground xPos: " + ground.getPosition().x + "Ground Height: " + ground.getHeight() + "Ground width: " + ground.getWidth());
            ground2 = new Ground(map.generateNext(ground).createPlatform(world));
            System.out.println("Ground xPos: " + ground2.getPosition().x + "Ground Height: " + ground2.getHeight() + "Ground width: " + ground2.getWidth());
        }
    }



    private void updateGround(){
        if(isActorOffScreen(grounds.get(0))) {
            grounds.remove(0).remove();
            //Ground ground = new Ground(map.generateNext(grounds.get(grounds.size()-1)).createPlatform(world));
            Ground ground = new Ground(map.next().createPlatform(world));
            addActor(ground);
            grounds.add(ground);
        }

        /*
        if (grounds[1].body.getPosition().x < -Constants.GROUND_WIDTH) {
            for (int i = 0; i < grounds.length - 1; i++) {
                grounds[i] = grounds[i + 1];
                Maps.platforms[i] = Maps.platforms[i + 1];

            }
            Maps.platforms[Maps.platforms.length - 1] = new Platform(1, Maps.platforms[Maps.platforms.length - 2].xPos + 1.5f);
            Ground ground = new Ground(Maps.platforms[Maps.platforms.length - 1].createPlatform(world));
            addActor(ground);

            grounds[grounds.length - 1] = ground;
        }

        for(Actor actor : this.getActors())
        {
            Vector3 windowCoordinates = new Vector3(actor.getX(), actor.getY(), 0);
            camera.project(windowCoordinates);
            if(windowCoordinates.x + actor.getWidth() < 0)
                actor.remove();
        }
        */
    }

    private boolean isActorOffScreen(GameActor actor) {
        return actor.getPosition().x + actor.getWidth() / 2 < 0;
//        Vector3 windowCoordinates = new Vector3(actor.getX(), actor.getY(), 0);
//        camera.project(windowCoordinates);
//        return windowCoordinates.x + actor.getWidth() < 0;
    }

    /**
     * Sets runner field to new body with fields specified in WorldUtils file.
     * Adds runner field to actor list
     */
    private void setUpRunner() {
        runner = new com.puffin.runner.Runner(WorldUtils.createRunner(world));
        addActor(runner);
    }

    private void setUpProjectile(float x, float y) {

        if(accumulate-accumulate2>= com.puffin.util.Constants.FIRE_DELAY) {
            Projectile projectile = new Projectile(WorldUtils.createProjectile(world, runner), x, y, runner);
            addActor(projectile);
            projectile.getBody().setLinearVelocity(projectile.linear_velocity.scl(com.puffin.util.Constants.PROJECTILE_SPEED));
            accumulate2 = accumulate;
        }

    }

    /**
     * Sets up new Orthographic camera and updates
     */
    private void setupCamera() {
        camera = new OrthographicCamera(com.puffin.util.Constants.VIEWPORT_WIDTH, com.puffin.util.Constants.VIEWPORT_HEIGHT);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0f);
        camera.update();
    }

    private void setupTouchControlAreas() {
        touchPoint = new Vector3();
        screenLeftSide = new Rectangle(0, 0, getCamera().viewportWidth / 2, getCamera().viewportHeight);
        screenRightSide = new Rectangle(getCamera().viewportWidth/2, 0, getCamera().viewportWidth/2, getCamera().viewportHeight);
        Gdx.input.setInputProcessor(this);
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        // Fixed timestep
        accumulator += delta;
        accumulate += delta;
        while (accumulator >= delta) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }
        updateGround();
        //TODO: Implement interpolation

    }

    @Override
    public void draw() {
        super.draw();
        renderer.render(world, camera.combined);

        sb.begin();
        //sb.draw(runner.getUserData().getTexture(), runner.getPosition().x, runner.getPosition().y, 300, 300);
        Sprite runnerSprite = runner.getUserData().getSprite();
        runnerSprite.setPosition(runner.getPosition().x / Constants.VIEWPORT_WIDTH * Gdx.graphics.getWidth() - runnerSprite.getWidth() / 2,
                (runner.getPosition().y - Constants.RUNNER_HEIGHT / 2) / Constants.VIEWPORT_HEIGHT * Gdx.graphics.getHeight());
        //runnerSprite.setOrigin(runnerSprite.getX(), runnerSprite.getY());
        //runnerSprite.setScale(.5f, .5f);
        runnerSprite.setSize(200, 200);
        runnerSprite.draw(sb);
        sb.end();
    }

    /**
     * If screen is touched on the left side then calls jump in runner field
     * @param x x integer coordinate of user's touch
     * @param y y integer coordinate of user's touch
     * @param pointer Not sure what this does
     * @param button Not sure what this does
     * @return boolean calls super function
     */
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        // Need to get the actual coordinates
        translateScreenToWorldCoordinates(x, y);

        if (leftSideTouched(touchPoint.x, touchPoint.y)) {
            runner.jump();
        }


        if (rightSideTouched(touchPoint.x, touchPoint.y))
        {
            setUpProjectile(touchPoint.x, touchPoint.y);
        }
        return super.touchDown(x, y, pointer, button);
    }

    private boolean leftSideTouched(float x, float y) {
        return screenLeftSide.contains(x, y);
    }
    private boolean rightSideTouched(float x, float y) {return screenRightSide.contains(x, y); }
    private void translateScreenToWorldCoordinates(int x, int y) {
        getCamera().unproject(touchPoint.set(x, y, 0));
    }

    @Override
    public void beginContact(Contact contact) {

        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();

        if ((BodyUtils.bodyIsRunner(a) && BodyUtils.bodyIsGround(b)) ||
                (BodyUtils.bodyIsGround(a) && BodyUtils.bodyIsRunner(b))) {
            runner.landed();
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
